package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.dto.LoginDTO;
import com.wealth.common.utils.JwtUtil;
import com.wealth.common.utils.RedisUtil;
import com.wealth.platform.system.entity.UmsAdmin;
import com.wealth.platform.system.entity.UmsAdminRoleRelation;
import com.wealth.platform.system.entity.UmsResource;
import com.wealth.platform.system.entity.UmsRoleResourceRelation;
import com.wealth.platform.system.mapper.UmsAdminMapper;
import com.wealth.platform.system.service.UmsAdminRoleRelationService;
import com.wealth.platform.system.service.UmsAdminService;
import com.wealth.platform.system.service.UmsResourceService;
import com.wealth.platform.system.service.UmsRoleResourceRelationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UmsAdminServiceImpl extends ServiceImpl<UmsAdminMapper, UmsAdmin> implements UmsAdminService {

    /** 最大登录失败次数 */
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    /** 锁定时间（分钟） */
    private static final long LOCK_DURATION_MINUTES = 15;
    /** 失败计数 TTL（分钟） */
    private static final long FAIL_COUNT_TTL_MINUTES = 15;
    /** 验证码 TTL（分钟） */
    private static final long CAPTCHA_TTL_MINUTES = 5;

    private static final String KEY_LOGIN_FAIL_COUNT = "login:fail:count:";
    private static final String KEY_LOGIN_LOCKED = "login:locked:";
    private static final String KEY_CAPTCHA = "captcha:";

    private final JwtUtil jwtUtil;
    private final UmsResourceService resourceService;
    private final UmsAdminRoleRelationService adminRoleRelationService;
    private final UmsRoleResourceRelationService roleResourceRelationService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;

    public UmsAdminServiceImpl(JwtUtil jwtUtil, UmsResourceService resourceService,
                               UmsAdminRoleRelationService adminRoleRelationService,
                               UmsRoleResourceRelationService roleResourceRelationService,
                               BCryptPasswordEncoder passwordEncoder,
                               RedisUtil redisUtil) {
        this.jwtUtil = jwtUtil;
        this.resourceService = resourceService;
        this.adminRoleRelationService = adminRoleRelationService;
        this.roleResourceRelationService = roleResourceRelationService;
        this.passwordEncoder = passwordEncoder;
        this.redisUtil = redisUtil;
    }

    @Override
    public String login(LoginDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new ServiceException(401, "用户名密码不能为空");
        }

        // 1. 校验验证码（如果提供了 captchaKey）
        if (StringUtils.hasText(dto.getCaptchaKey())) {
            validateCaptcha(dto.getCaptchaKey(), dto.getCaptchaCode());
        }

        // 2. 检查账号是否被锁定
        String lockKey = KEY_LOGIN_LOCKED + dto.getUsername();
        if (Boolean.TRUE.equals(redisUtil.hasKey(lockKey))) {
            throw new ServiceException(401, "账号已被锁定，请" + LOCK_DURATION_MINUTES + "分钟后再试");
        }

        // 3. 查询用户
        UmsAdmin admin = lambdaQuery()
                .eq(UmsAdmin::getUsername, dto.getUsername())
                .one();

        if (admin == null) {
            recordFailedAttempt(dto.getUsername());
            throw new ServiceException(401, "用户名或密码错误");
        }

        // 4. 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            recordFailedAttempt(dto.getUsername());
            throw new ServiceException(401, "用户名或密码错误");
        }

        // 5. 登录成功，清除失败记录
        clearFailedAttempts(dto.getUsername());

        return jwtUtil.generateToken(admin.getUsername());
    }

    /**
     * 记录登录失败次数，达到阈值时锁定账号。
     */
    private void recordFailedAttempt(String username) {
        String countKey = KEY_LOGIN_FAIL_COUNT + username;
        Long count = redisUtil.increment(countKey);
        if (count == null) return;

        // 第一次设置 TTL（防止永久 key）
        if (count == 1) {
            redisUtil.expire(countKey, FAIL_COUNT_TTL_MINUTES, TimeUnit.MINUTES);
        }

        if (count >= MAX_LOGIN_ATTEMPTS) {
            String lockKey = KEY_LOGIN_LOCKED + username;
            redisUtil.set(lockKey, "1", LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
            // 清理计数（锁定期间不需要计数了）
            redisUtil.delete(countKey);
        }
    }

    /**
     * 登录成功时清除失败记录和锁定标记。
     */
    private void clearFailedAttempts(String username) {
        redisUtil.delete(KEY_LOGIN_FAIL_COUNT + username);
        redisUtil.delete(KEY_LOGIN_LOCKED + username);
    }

    /**
     * 校验验证码。
     */
    private void validateCaptcha(String captchaKey, String captchaCode) {
        if (!StringUtils.hasText(captchaKey) || !StringUtils.hasText(captchaCode)) {
            throw new ServiceException(400, "验证码不能为空");
        }
        String redisKey = KEY_CAPTCHA + captchaKey;
        String stored = (String) redisUtil.get(redisKey);
        if (stored == null) {
            throw new ServiceException(400, "验证码已过期，请重新获取");
        }
        // 一次性使用，立即删除
        redisUtil.delete(redisKey);
        if (!stored.equalsIgnoreCase(captchaCode.trim())) {
            throw new ServiceException(400, "验证码错误");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createAdmin(UmsAdmin admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return save(admin);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAdmin(UmsAdmin admin) {
        admin.setPassword(null);
        return updateById(admin);
    }

    // ================== 权限查询方法 ==================
    @Override
    public List<String> getResourceUrlsByIds(List<Long> resourceIds) {
        return resourceService.lambdaQuery()
                .in(UmsResource::getId, resourceIds)
                .list()
                .stream()
                .map(UmsResource::getUrl)
                .collect(Collectors.toList());
    }

    /** 判断指定 admin 是否有权访问某 URI（Ant 风格匹配） */
    @Override
    public boolean hasPermission(Long adminId, String uri) {
        List<Long> roleIds = adminRoleRelationService.getRoleIdByAdminId(adminId);
        if (roleIds.isEmpty()) return false;

        List<Long> resourceIds = roleResourceRelationService.getResourceIdByRoleIds(roleIds);
        if (resourceIds.isEmpty()) return false;

        List<String> urlPatterns = getResourceUrlsByIds(resourceIds);

        AntPathMatcher pathMatcher = new AntPathMatcher();
        return urlPatterns.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }
}
