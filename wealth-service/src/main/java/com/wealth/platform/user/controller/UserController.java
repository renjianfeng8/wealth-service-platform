package com.wealth.platform.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.dto.LoginDTO;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.common.utils.BeanConvertUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import com.wealth.platform.user.dto.ResetPasswordDTO;
import com.wealth.platform.user.dto.UserDTO;
import com.wealth.platform.user.entity.User;
import com.wealth.platform.user.service.UserService;
import com.wealth.platform.user.vo.UserVO;
import com.wealth.platform.user.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "系统用户管理", description = "系统用户相关接口")
@RequestMapping("/user")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    public Result<UserVO> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(BeanConvertUtil.convert(user, UserVO.class));
    }

    @GetMapping
    @Operation(summary = "查询用户列表")
    public Result<List<UserVO>> list() {
        List<User> list = userService.page(new Page<>(1, 1000)).getRecords();
        List<UserVO> voList = BeanConvertUtil.convertList(list, UserVO.class);
        return Result.success(voList);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询用户")
    public Result<IPage<UserVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        IPage<User> page = userService.pageWithFilter(pageNum, pageSize, username, status);
        return Result.success(BeanConvertUtil.convertPage(page, UserVO.class));
    }

    @PostMapping
    @Operation(summary = "新增用户")
    @AuditLog(module = "用户管理", operation = "新增用户")
    public Result<Boolean> create(@Valid @RequestBody UserDTO dto) {
        User user = BeanConvertUtil.convert(dto, User.class);
        return Result.success(userService.save(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改用户")
    @AuditLog(module = "用户管理", operation = "修改用户")
    public Result<Boolean> update(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO dto) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        BeanConvertUtil.copyNonNullProperties(dto, user);
        user.setPassword(null); // 禁止通过更新接口修改密码
        user.setId(id);
        return Result.success(userService.updateById(user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @AuditLog(module = "用户管理", operation = "删除用户")
    public Result<Boolean> delete(@PathVariable Long id) {
        User existing = userService.getById(id);
        if (existing == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(userService.removeById(id));
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除")
    @AuditLog(module = "用户管理", operation = "批量删除用户")
    public Result<Boolean> deleteBatch(@Valid @NotEmpty(message = "ID列表不能为空") @RequestBody List<Long> ids) {
        return Result.success(userService.removeByIds(ids));
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    @AuditLog(module = "用户管理", operation = "用户注册")
    @AntiReplay
    public Result<Boolean> register(@Valid @RequestBody UserDTO dto) {
        User user = BeanConvertUtil.convert(dto, User.class);
        return Result.success(userService.register(user));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    @AuditLog(module = "用户管理", operation = "用户登录")
    public ResponseEntity<Result<LoginVO>> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO loginVO = userService.login(dto);
        ResponseCookie cookie = ResponseCookie.from("wealth_token", loginVO.getToken())
                .httpOnly(true)
                .path("/")
                .maxAge(1800)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Result.success(loginVO));
    }

    @PostMapping("/identify-login")
    @Operation(summary = "统一登录（自动识别用户类型）")
    @AuditLog(module = "用户管理", operation = "统一登录")
    public ResponseEntity<Result<LoginVO>> identifyLogin(@Valid @RequestBody LoginDTO dto) {
        LoginVO loginVO = userService.identifyLogin(dto);
        ResponseCookie cookie = ResponseCookie.from("wealth_token", loginVO.getToken())
                .httpOnly(true)
                .path("/")
                .maxAge(1800)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Result.success(loginVO));
    }

    @PostMapping("/resetPassword")
    @Operation(summary = "重置密码")
    @AuditLog(module = "用户管理", operation = "重置密码")
    @AntiReplay
    public Result<Boolean> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        String oldPassword = dto.getOldPassword();
        User user = new User();
        user.setId(dto.getId());
        user.setPassword(dto.getPassword());
        return Result.success(userService.resetPassword(user, oldPassword));
    }
}
