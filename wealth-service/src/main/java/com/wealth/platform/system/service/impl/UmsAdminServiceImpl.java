package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wealth.common.contract.AdminIdentityProvider;
import com.wealth.common.dto.AdminIdentityDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.dto.LoginDTO;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.common.utils.JwtUtil;
import com.wealth.common.utils.JwtUtil.TokenPair;
import com.wealth.common.utils.RedisUtil;
import com.wealth.common.utils.LikeUtil;
import com.wealth.platform.system.entity.UmsAdmin;
import com.wealth.platform.system.entity.UmsResource;
import com.wealth.platform.system.mapper.UmsAdminMapper;
import com.wealth.platform.system.service.PermissionCacheService;
import com.wealth.platform.system.service.UmsAdminService;
import com.wealth.platform.system.service.UmsResourceService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UmsAdminServiceImpl extends ServiceImpl<UmsAdminMapper, UmsAdmin>
        implements UmsAdminService, AdminIdentityProvider {

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
    private static final String KEY_REFRESH_JTI = "refresh:jti:";
    private static final String KEY_REFRESH_COMPROMISED = "refresh:compromised:";
    private static final String KEY_REFRESH_BLACKLIST = "refresh:blacklist:";

    private final JwtUtil jwtUtil;
    private final UmsResourceService resourceService;
    private final PermissionCacheService permissionCacheService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;

    @Override
    public TokenPair login(LoginDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new ServiceException(401, "用户名密码不能为空");
        }

        // 1. 校验验证码（如果提供了 captchaKey）
        if (StringUtils.hasText(dto.getCaptchaKey())) {
            validateCaptcha(dto.getCaptchaKey(), dto.getCaptchaCode());
        }

        // 2. 检查账号是否被锁定（Redis 不可用时不阻塞登录）
        String lockKey = KEY_LOGIN_LOCKED + dto.getUsername();
        Boolean isLocked = redisUtil.safeExecute(() -> redisUtil.hasKey(lockKey), false, "跳过锁定检查");
        if (Boolean.TRUE.equals(isLocked)) {
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

        // 6. 更新最后登录时间
        admin.setLoginTime(LocalDateTime.now());
        updateById(admin);

        // 7. 生成双 Token（access_token + refresh_token）
        TokenPair pair = jwtUtil.generateTokenPair(admin.getUsername());
        // 将 refresh_token 的 jti 存入 Redis（TTL 7 天），Redis 不可用时不阻塞登录
        String jti = jwtUtil.getTokenIdFromToken(pair.refreshToken());
        redisUtil.safeExecuteVoid(() -> redisUtil.set(KEY_REFRESH_JTI + jti, admin.getUsername(), 7, TimeUnit.DAYS), "refresh_token 未持久化");
        return pair;
    }

    @Override
    public TokenPair refreshToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ServiceException(401, "无效的 refreshToken");
        }
        String refreshToken = authHeader.substring(7);
        if (!StringUtils.hasText(refreshToken)) {
            throw new ServiceException(400, "refreshToken 不能为空");
        }

        // 1. 校验 refresh_token 签名和有效期
        String username;
        String jti;
        try {
            username = jwtUtil.getUsernameFromToken(refreshToken);
            jti = jwtUtil.getTokenIdFromToken(refreshToken);
        } catch (Exception e) {
            throw new ServiceException(401, "refreshToken 无效或已过期");
        }

        // 2. 检查 refresh_token 是否已被注销（退出登录时加入黑名单）
        Boolean blacklisted = redisUtil.safeExecute(() -> redisUtil.hasKey(KEY_REFRESH_BLACKLIST + jti), false, "跳过黑名单检查");
        if (Boolean.TRUE.equals(blacklisted)) {
            throw new ServiceException(401, "refreshToken 已注销，请重新登录");
        }

        // 3. 检查该用户是否被标记为"疑似被盗"，Redis 不可用时跳过检查
        Boolean compromised = redisUtil.safeExecute(() -> redisUtil.hasKey(KEY_REFRESH_COMPROMISED + username), false, "跳过被盗检测");
        if (Boolean.TRUE.equals(compromised)) {
            log.warn("用户 {} 的 refresh_token 疑似被盗，已禁止所有 refresh 操作", username);
            throw new ServiceException(401, "账户存在安全风险，请重新登录");
        }

        // 4. 防并发竞争锁（以 jti 为锁 key，30 秒自动过期）
        String lockKey = "refresh:lock:" + jti;
        Boolean locked = redisUtil.safeExecute(() -> redisUtil.setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS), false, "跳过并发锁");
        if (Boolean.FALSE.equals(locked)) {
            throw new ServiceException(429, "正在刷新 token，请稍后重试");
        }

        // 5. 检查是否已吊销（Redis 中是否存在），Redis 不可用时要求重新登录
        Boolean exists = redisUtil.safeExecute(() -> redisUtil.hasKey(KEY_REFRESH_JTI + jti), null, "无法校验 refresh_token");
        if (exists == null) {
            throw new ServiceException(401, "认证服务暂时不可用，请重新登录");
        }
        if (Boolean.FALSE.equals(exists)) {
            // 已被轮换过的 refresh_token 再次被使用 → 疑似被盗
            // 标记该用户，禁用该用户所有 refresh_token
            log.warn("检测到已轮换的 refresh_token 被再次使用，用户 {} 疑似被盗", username);
            redisUtil.safeExecuteVoid(() -> redisUtil.set(KEY_REFRESH_COMPROMISED + username, "1", 7, TimeUnit.DAYS), "无法标记被盗状态");
            throw new ServiceException(401, "refreshToken 已吊销，请重新登录");
        }

        // 6. 吊销旧 refresh_token（一次性使用，防重放）
        redisUtil.safeExecuteVoid(() -> redisUtil.delete(KEY_REFRESH_JTI + jti), "跳过旧 refresh_token 删除");

        // 7. 生成新的 token 对
        TokenPair pair = jwtUtil.generateTokenPair(username);
        String newJti = jwtUtil.getTokenIdFromToken(pair.refreshToken());
        redisUtil.safeExecuteVoid(() -> redisUtil.set(KEY_REFRESH_JTI + newJti, username, 7, TimeUnit.DAYS), "新 refresh_token 未持久化");

        // 8. 登录成功则清除该用户被盗标记（如有）
        redisUtil.safeExecuteVoid(() -> redisUtil.delete(KEY_REFRESH_COMPROMISED + username), "无法清除被盗标记");

        return pair;
    }

    /**
     * 退出登录：将 refresh_token 加入黑名单，阻止后续刷新。
     */
    @Override
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        String refreshToken = authHeader.substring(7);
        try {
            String jti = jwtUtil.getTokenIdFromToken(refreshToken);
            redisUtil.set(KEY_REFRESH_BLACKLIST + jti, "1", 7, TimeUnit.DAYS);
            log.info("refresh_token 已加入黑名单，jti={}", jti);
        } catch (Exception e) {
            log.warn("退出登录处理异常: {}", e.getMessage());
        }
    }

    /**
     * 清除指定管理员的权限缓存，下次请求重新从数据库加载。
     */
    @Override
    public void clearPermissionCache(Long adminId) {
        permissionCacheService.clearCache(adminId);
    }

    /**
     * 记录登录失败次数，达到阈值时锁定账号。
     * Redis 不可用时跳过计数，不阻塞登录。
     */
    private void recordFailedAttempt(String username) {
        String countKey = KEY_LOGIN_FAIL_COUNT + username;
        Long count = redisUtil.safeExecute(() -> redisUtil.increment(countKey), null, "跳过登录失败计数");
        if (count == null) return;

        if (count == 1) {
            redisUtil.safeExecuteVoid(() -> redisUtil.expire(countKey, FAIL_COUNT_TTL_MINUTES, TimeUnit.MINUTES), "无法设置失败计数 TTL");
        }

        if (count >= MAX_LOGIN_ATTEMPTS) {
            String lockKey = KEY_LOGIN_LOCKED + username;
            redisUtil.safeExecuteVoid(() -> {
                redisUtil.set(lockKey, "1", LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
                redisUtil.delete(countKey);
            }, "无法锁定账号");
        }
    }

    private void clearFailedAttempts(String username) {
        redisUtil.safeExecuteVoid(() -> {
            redisUtil.delete(KEY_LOGIN_FAIL_COUNT + username);
            redisUtil.delete(KEY_LOGIN_LOCKED + username);
        }, "无法清除登录失败记录");
    }

    private void validateCaptcha(String captchaKey, String captchaCode) {
        if (!StringUtils.hasText(captchaKey) || !StringUtils.hasText(captchaCode)) {
            throw new ServiceException(400, "验证码不能为空");
        }
        String redisKey = KEY_CAPTCHA + captchaKey;
        String stored;
        try {
            stored = (String) redisUtil.get(redisKey);
        } catch (DataAccessException e) {
            log.warn("Redis 不可用，跳过验证码校验: {}", e.getMessage());
            return;
        }
        if (stored == null) {
            throw new ServiceException(400, "验证码已过期，请重新获取");
        }
        redisUtil.delete(redisKey);
        if (!stored.equalsIgnoreCase(captchaCode.trim())) {
            throw new ServiceException(400, "验证码错误");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createAdmin(UmsAdmin admin) {
        long count = lambdaQuery().eq(UmsAdmin::getUsername, admin.getUsername()).count();
        if (count > 0) {
            throw new ServiceException(400, "管理员用户名已存在");
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        try {
            return save(admin);
        } catch (DataIntegrityViolationException e) {
            throw new ServiceException(400, "管理员用户名已存在");
        }
    }

    @Override
    public List<String> getResourceUrlsByIds(List<Long> resourceIds) {
        return resourceService.lambdaQuery()
                .in(UmsResource::getId, resourceIds)
                .list()
                .stream()
                .map(UmsResource::getUrl)
                .collect(Collectors.toList());
    }

    @Override
    public IPage<UmsAdmin> pageWithFilter(Integer pageNum, Integer pageSize, String username, Integer status) {
        Page<UmsAdmin> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UmsAdmin> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(UmsAdmin::getUsername, LikeUtil.escape(username));
        }
        if (status != null) {
            wrapper.eq(UmsAdmin::getStatus, status);
        }
        wrapper.orderByDesc(UmsAdmin::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resetPassword(Long id, String oldPassword, String newPassword) {
        UmsAdmin admin = getById(id);
        if (admin == null) {
            throw new ServiceException(404, "管理员不存在");
        }
        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            throw new ServiceException(400, "原密码错误");
        }
        admin.setPassword(passwordEncoder.encode(newPassword));
        return updateById(admin);
    }

    @Override
    public boolean hasPermission(Long adminId, String uri) {
        return permissionCacheService.hasPermission(adminId, uri);
    }

    @Override
    public boolean checkPermission(String uri, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return false;
        }
        String username = jwtUtil.getUsernameFromToken(token);
        UmsAdmin admin = lambdaQuery()
                .eq(UmsAdmin::getUsername, username)
                .eq(UmsAdmin::getDelFlag, 0)
                .one();
        if (admin == null) {
            return false;
        }
        return hasPermission(admin.getId(), uri);
    }

    @Override
    public AdminIdentityDTO findByUsername(String username) {
        UmsAdmin admin = lambdaQuery()
                .eq(UmsAdmin::getUsername, username)
                .one();
        if (admin == null) {
            return null;
        }
        AdminIdentityDTO dto = BeanConvertUtil.convert(admin, AdminIdentityDTO.class);
        dto.setNickname(admin.getNickName());
        return dto;
    }
}
