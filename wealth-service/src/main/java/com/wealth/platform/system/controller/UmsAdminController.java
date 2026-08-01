package com.wealth.platform.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.dto.LoginDTO;
import com.wealth.common.result.Result;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.common.utils.JwtUtil.TokenPair;
import com.wealth.platform.system.dto.UmsAdminDTO;
import com.wealth.platform.system.dto.UmsAdminResetPasswordDTO;
import com.wealth.platform.system.entity.UmsAdmin;
import com.wealth.platform.system.service.UmsAdminService;
import com.wealth.platform.system.vo.UmsAdminVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/system/umsAdmin")
@Tag(name = "后台管理员管理")
@Validated
@RequiredArgsConstructor
public class UmsAdminController {

    private final UmsAdminService umsAdminService;

    @PostMapping("/login")
    @Operation(summary = "管理员登录（返回 access_token + refresh_token）")
    @AuditLog(module = "系统管理", operation = "管理员登录")
    @AntiReplay
    public ResponseEntity<Result<TokenPair>> login(@Valid @RequestBody LoginDTO dto) {
        TokenPair tokenPair = umsAdminService.login(dto);

        ResponseCookie cookie = ResponseCookie.from("wealth_token", tokenPair.accessToken())
                .httpOnly(true)
                .path("/")
                .maxAge(tokenPair.expiresIn() / 1000)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Result.success(tokenPair));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token（用 refresh_token 换取新的 access_token + refresh_token）")
    public Result<TokenPair> refresh(@RequestHeader("Authorization") String authHeader) {
        return Result.success(umsAdminService.refreshToken(authHeader));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询")
    public Result<UmsAdminVO> getById(@PathVariable Long id) {
        return Result.success(umsAdminService.getAdminById(id));
    }

    @GetMapping
    @Operation(summary = "列表查询")
    public Result<List<UmsAdminVO>> list(
            @Min(1) @RequestParam(defaultValue = "1") Integer pageNum,
            @Min(1) @Max(200) @RequestParam(defaultValue = "20") Integer pageSize) {
        List<UmsAdmin> list = umsAdminService.page(new Page<>(pageNum, pageSize)).getRecords();
        return Result.success(BeanConvertUtil.convertList(list, UmsAdminVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public Result<IPage<UmsAdminVO>> page(
            @Min(1) @RequestParam(defaultValue = "1") Integer pageNum,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        IPage<UmsAdmin> page = umsAdminService.pageWithFilter(pageNum, pageSize, username, status);
        return Result.success(BeanConvertUtil.convertPage(page, UmsAdminVO.class));
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
        return Result.success(umsAdminService.updateAdmin(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除管理员")
    @AuditLog(module = "系统管理", operation = "删除管理员")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(umsAdminService.deleteAdmin(id));
    }

    @GetMapping("/checkPermission")
    @Operation(summary = "校验权限（Feign调用）", hidden = true)
    public Result<Boolean> checkPermission(
            @RequestParam String uri,
            @RequestHeader("Authorization") String authHeader) {
        return Result.success(umsAdminService.checkPermission(uri, authHeader));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录（将 refresh_token 加入黑名单）")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        umsAdminService.logout(authHeader);
        return Result.success(null);
    }

    @PostMapping("/resetPassword")
    @Operation(summary = "重置密码")
    @AuditLog(module = "系统管理", operation = "重置密码")
    @AntiReplay
    public Result<Boolean> resetPassword(@Valid @RequestBody UmsAdminResetPasswordDTO dto) {
        return Result.success(umsAdminService.resetPassword(dto.getId(), dto.getOldPassword(), dto.getPassword()));
    }
}
