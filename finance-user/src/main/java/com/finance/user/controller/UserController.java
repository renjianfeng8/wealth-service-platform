package com.finance.platform.user.controller;

import com.finance.common.result.Result;
import com.finance.platform.user.entity.User;
import com.finance.platform.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "系统用户管理", description = "系统用户相关接口")
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    // 构造器：用于依赖注入
    /**
     * 系统用户控制器构造方法
     *
     * @param userService 系统用户业务服务
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 根据ID查询系统用户信息
     *
     * @param id 系统用户ID
     * @return 查询结果
     */
    @Operation(summary = "根据ID查询系统用户")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * 查询系统用户列表（未分页）
     *
     * @return 用户列表
     */
    @Operation(summary = "查询系统用户列表")
    @GetMapping
    public Result<List<User>> list() {
        return Result.success(userService.list());
    }

    /**
     * 创建系统用户
     *
     * @param user 系统用户信息
     * @return 是否创建成功
     */
    @Operation(summary = "创建系统用户")
    @PostMapping
    public Result<Boolean> create(@RequestBody User user) {
        boolean saved = userService.save(user);
        return Result.success(saved);
    }

    /**
     * 更新系统用户信息
     *
     * @param id 系统用户ID
     * @param user 系统用户信息
     * @return 是否更新成功
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
     *
     * @param id 系统用户ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除系统用户（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = userService.removeById(id);
        return Result.success(removed);
    }
}