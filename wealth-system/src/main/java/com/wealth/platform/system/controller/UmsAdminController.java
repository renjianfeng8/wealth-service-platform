package com.wealth.platform.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.dto.LoginDTO;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.common.utils.JwtUtil.TokenPair;
import com.wealth.platform.system.dto.UmsAdminDTO;
import com.wealth.platform.system.entity.UmsAdmin;
import com.wealth.platform.system.service.UmsAdminService;
import com.wealth.platform.system.vo.UmsAdminVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/umsAdmin")
@Tag(name = "后台管理员管理")
@Validated
public class UmsAdminController {

    private final UmsAdminService umsAdminService;
    private final com.wealth.common.utils.JwtUtil jwtUtil;

    public UmsAdminController(UmsAdminService umsAdminService,
                              com.wealth.common.utils.JwtUtil jwtUtil) {
        this.umsAdminService = umsAdminService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    @Operation(summary = "管理员登录（返回 access_token + refresh_token）")
    @AuditLog(module = "系统管理", operation = "管理员登录")
    public Result<TokenPair> login(@Valid @RequestBody LoginDTO dto, HttpServletResponse servletResponse) {
        TokenPair tokenPair = umsAdminService.login(dto);
        // 设置 httpOnly Cookie（防 XSS 窃取 JWT）
        Cookie cookie = new Cookie("wealth_token", tokenPair.accessToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (tokenPair.expiresIn() / 1000));
        servletResponse.addCookie(cookie);
        return Result.success(tokenPair);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token（用 refresh_token 换取新的 access_token + refresh_token）")
    public Result<TokenPair> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        String refreshToken = authHeader.substring(7);
        return Result.success(umsAdminService.refreshToken(refreshToken));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询")
    public Result<UmsAdminVO> getById(@PathVariable Long id) {
        UmsAdmin admin = umsAdminService.getById(id);
        if (admin == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(BeanConvertUtil.convert(admin, UmsAdminVO.class));
    }

    @GetMapping
    @Operation(summary = "列表查询")
    public Result<List<UmsAdminVO>> list() {
        List<UmsAdmin> list = umsAdminService.list();
        return Result.success(BeanConvertUtil.convertList(list, UmsAdminVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public Result<IPage<UmsAdminVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<UmsAdmin> page = new Page<>(pageNum, pageSize);
        return Result.success(BeanConvertUtil.convertPage(umsAdminService.page(page), UmsAdminVO.class));
    }

    @PostMapping
    @Operation(summary = "新增管理员")
    @AuditLog(module = "系统管理", operation = "新增管理员")
    @AntiReplay
    public Result<Boolean> create(@Valid @RequestBody UmsAdminDTO dto) {
        UmsAdmin admin = BeanConvertUtil.convert(dto, UmsAdmin.class);
        return Result.success(umsAdminService.createAdmin(admin));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改管理员")
    @AuditLog(module = "系统管理", operation = "修改管理员")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody UmsAdminDTO dto) {
        UmsAdmin admin = BeanConvertUtil.convert(dto, UmsAdmin.class);
        admin.setId(id);
        return Result.success(umsAdminService.updateAdmin(admin));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除管理员")
    @AuditLog(module = "系统管理", operation = "删除管理员")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(umsAdminService.removeById(id));
    }

    @GetMapping("/checkPermission")
    @Operation(summary = "校验权限（Feign调用）", hidden = true)
    public Result<Boolean> checkPermission(
            @RequestParam String uri,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.success(false);
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return Result.success(false);
        }
        String username = jwtUtil.getUsernameFromToken(token);
        UmsAdmin admin = umsAdminService.lambdaQuery()
                .eq(UmsAdmin::getUsername, username)
                .eq(UmsAdmin::getDelFlag, 0)
                .one();
        if (admin == null) {
            return Result.success(false);
        }
        return Result.success(umsAdminService.hasPermission(admin.getId(), uri));
    }
}
