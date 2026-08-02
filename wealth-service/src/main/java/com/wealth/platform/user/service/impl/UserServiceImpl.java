package com.wealth.platform.user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wealth.platform.common.base.BaseBizServiceImpl;
import com.wealth.common.auth.AuthSupport;
import com.wealth.common.constants.AuthConstant;
import com.wealth.common.contract.AdminIdentityProvider;
import com.wealth.common.dto.AdminIdentityDTO;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.common.utils.JwtUtil;
import com.wealth.common.utils.JwtUtil.TokenPair;
import com.wealth.common.utils.RedisUtil;
import com.wealth.common.dto.LoginDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.platform.system.service.CaptchaService;
import com.wealth.platform.user.dto.UserDTO;
import com.wealth.platform.user.entity.User;
import com.wealth.platform.user.mapper.UserMapper;
import com.wealth.platform.user.service.UserService;
import com.wealth.platform.user.vo.LoginVO;
import com.wealth.platform.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseBizServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AdminIdentityProvider adminIdentityProvider;
    private final CaptchaService captchaService;
    private final RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(UserDTO dto) {
        User user = BeanConvertUtil.convert(dto, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean register(UserDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new ServiceException(400, "用户名/密码不能为空");
        }
        // 可选验证码校验（提供 captchaKey 才校验，向后兼容）
        if (StringUtils.hasText(dto.getCaptchaKey())) {
            captchaService.verify(dto.getCaptchaKey(), dto.getCaptchaCode());
        }
        checkUnique(User::getUsername, dto.getUsername(), "用户名已存在");
        User user = BeanConvertUtil.convert(dto, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return this.save(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        AuthSupport.assertCredentialsPresent(dto.getUsername(), dto.getPassword());

        User dbUser = this.lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .one();

        if (dbUser == null) {
            throw new ServiceException(401, "用户名或密码错误");
        }

        AuthSupport.verifyCredentials(passwordEncoder, dbUser.getStatus(), dbUser.getPassword(), dto.getPassword());

        // /user/login 为兼容遗留端点，仅签 access token（refreshToken 为空）；统一登录走 identifyLogin
        return new LoginVO(jwtUtil.generateToken(dbUser.getUsername(), "user"), dbUser.getId(), dbUser.getNickname(), "user", jwtUtil.getAccessExpire() / 1000, null);
    }

    @Override
    public LoginVO identifyLogin(LoginDTO dto) {
        AuthSupport.assertCredentialsPresent(dto.getUsername(), dto.getPassword());

        // 0. 可选验证码校验（提供 captchaKey 才校验，向后兼容）
        if (StringUtils.hasText(dto.getCaptchaKey())) {
            captchaService.verify(dto.getCaptchaKey(), dto.getCaptchaCode());
        }

        // 1. 先查 ums_admin 表 — 判断是否为管理员（delFlag=0）
        AdminIdentityDTO admin = adminIdentityProvider.findByUsername(dto.getUsername());
        if (admin != null) {
            AuthSupport.verifyCredentials(passwordEncoder, admin.getStatus(), admin.getPassword(), dto.getPassword());
            TokenPair pair = issueUserTokenPair(admin.getUsername(), "admin");
            return new LoginVO(pair.accessToken(), admin.getId(), admin.getNickname(), "admin",
                    jwtUtil.getAccessExpire() / 1000, pair.refreshToken());
        }

        // 2. 再查 user 表 — 判断是否为普通用户
        User user = this.lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .one();
        if (user != null) {
            AuthSupport.verifyCredentials(passwordEncoder, user.getStatus(), user.getPassword(), dto.getPassword());
            TokenPair pair = issueUserTokenPair(user.getUsername(), "user");
            return new LoginVO(pair.accessToken(), user.getId(), user.getNickname(), "user",
                    jwtUtil.getAccessExpire() / 1000, pair.refreshToken());
        }

        // 3. 都没找到
        throw new ServiceException(401, "用户名或密码错误");
    }

    /**
     * 统一登录签发 token 对：access 保留 userType claim（供前端/审计），refresh 走标准长时效签发，
     * 并持久化 jti 到 Redis（与管理端登录同前缀），使现有 /system/umsAdmin/refresh 可对统一登录用户续期。
     */
    private TokenPair issueUserTokenPair(String username, String userType) {
        String accessToken = jwtUtil.generateToken(username, userType);
        String refreshToken = jwtUtil.generateRefreshToken(username);
        String jti = jwtUtil.getTokenIdFromToken(refreshToken);
        redisUtil.safeExecuteVoid(() -> redisUtil.set(
                AuthConstant.REFRESH_JTI_KEY_PREFIX + jti, username, 7, TimeUnit.DAYS),
                "refresh_token 未持久化");
        return new TokenPair(accessToken, refreshToken, jwtUtil.getAccessExpire());
    }

    @Override
    public IPage<User> pageWithFilter(Integer pageNum, Integer pageSize, String username, Integer status) {
        return pageWithFilter(pageNum, pageSize, orderByDesc(User::getCreateTime),
                like(User::getUsername, username), eq(User::getStatus, status));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resetPassword(User user, String oldPassword) {
        if (!StringUtils.hasText(user.getPassword())) {
            throw new ServiceException(400, "新密码不能为空");
        }
        if (!StringUtils.hasText(oldPassword)) {
            throw new ServiceException(400, "旧密码不能为空");
        }

        // 通过 username 查找用户（如果 id 为空），支持前端只传 username 的场景
        User dbUser;
        if (user.getId() != null) {
            dbUser = getById(user.getId());
        } else if (StringUtils.hasText(user.getUsername())) {
            dbUser = lambdaQuery().eq(User::getUsername, user.getUsername()).one();
        } else {
            throw new ServiceException(400, "用户标识不能为空");
        }

        if (dbUser == null) {
            throw new ServiceException(404, "用户不存在");
        }

        // 验证旧密码
        AuthSupport.verifyOldPasswordOrThrow(passwordEncoder, oldPassword, dbUser.getPassword(), "旧密码不正确");

        return this.lambdaUpdate()
                .eq(User::getId, dbUser.getId())
                .set(User::getPassword, passwordEncoder.encode(user.getPassword()))
                .update();
    }

    @Override
    public List<UserVO> getUserList(Integer pageNum, Integer pageSize) {
        return pageVoList(pageNum, pageSize, UserVO.class);
    }

    @Override
    public UserVO getUserById(Long id) {
        return getVoByIdOrThrow(id, UserVO.class, "用户");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(Long id, UserDTO dto) {
        User entity = getEntityOrThrow(id, "用户");
        BeanConvertUtil.copyNonNullProperties(dto, entity);
        entity.setPassword(null);
        entity.setId(id);
        return updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        return deleteWithCheck(id, "用户");
    }
}
