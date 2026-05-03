package com.finance.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.common.result.Result;
import com.finance.user.entity.User;
import com.finance.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "系统用户管理", description = "系统用户相关接口")
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 根据ID查询系统用户信息
     */
    @Operation(summary = "根据ID查询系统用户")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * 查询系统用户列表（未分页）
     */
    @Operation(summary = "查询系统用户列表")
    @GetMapping
    public Result<List<User>> list() {
        return Result.success(userService.list());
    }

    /**
     * 分页查询系统用户
     */
    @Operation(summary = "分页查询系统用户")
    @GetMapping("/page")
    public Result<IPage<User>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        return Result.success(userService.page(page));
    }

    /**
     * 创建系统用户
     */
    @Operation(summary = "创建系统用户")
    @PostMapping
    public Result<Boolean> create(@RequestBody User user) {
        boolean saved = userService.save(user);
        return Result.success(saved);
    }

    /**
     * 更新系统用户信息
     */
    @Operation(summary = "更新系统用户信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        boolean updated = userService.updateById(user);
        return Result.success(updated);
    }

    /**
     * 删除系统用户（逻辑删除）
     */
    @Operation(summary = "删除系统用户（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = userService.removeById(id);
        return Result.success(removed);
    }

    /**
     * 批量删除系统用户
     */
    @Operation(summary = "批量删除系统用户")
    @DeleteMapping("/batch")
    public Result<Boolean> deleteBatch(@RequestBody List<Long> ids) {
        boolean removed = userService.removeByIds(ids);
        return Result.success(removed);
    }

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Boolean> register(@RequestBody User user) {
        return Result.success(userService.register(user));
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<String> login(@RequestBody User user) {
        String token = userService.login(user);
        return Result.success(token);
    }

    /**
     * 重置密码
     */
    @Operation(summary = "重置密码")
    @PostMapping("/resetPassword")
    public Result<Boolean> resetPassword(@RequestBody User user) {
        return Result.success(userService.resetPassword(user));
    }

}