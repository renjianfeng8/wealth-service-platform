package com.finance.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.common.dto.LoginDTO;
import com.finance.common.result.Result;
import com.finance.common.result.ResultCode;
import com.finance.common.utils.BeanConvertUtil;
import com.finance.user.dto.UserDTO;
import com.finance.user.entity.User;
import com.finance.user.service.UserService;
import com.finance.user.vo.UserVO;
import com.finance.user.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "系统用户管理", description = "系统用户相关接口")
@RequestMapping
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
        List<User> list = userService.list();
        List<UserVO> voList = BeanConvertUtil.convertList(list, UserVO.class);
        return Result.success(voList);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询用户")
    public Result<IPage<UserVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        IPage<User> userPage = userService.page(page);

        Page<UserVO> voPage = new Page<>();
        org.springframework.beans.BeanUtils.copyProperties(userPage, voPage, "records");
        voPage.setRecords(BeanConvertUtil.convertList(userPage.getRecords(), UserVO.class));
        return Result.success(voPage);
    }

    @PostMapping
    @Operation(summary = "新增用户")
    public Result<Boolean> create(@Valid @RequestBody UserDTO dto) {
        User user = BeanConvertUtil.convert(dto, User.class);
        return Result.success(userService.save(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改用户")
    public Result<Boolean> update(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO dto) {
        User user = BeanConvertUtil.convert(dto, User.class);
        user.setId(id);
        user.setPassword(null); // 禁止通过更新接口修改密码，防止明文覆盖
        return Result.success(userService.updateById(user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除")
    public Result<Boolean> deleteBatch(@RequestBody List<Long> ids) {
        return Result.success(userService.removeByIds(ids));
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Boolean> register(@Valid @RequestBody UserDTO dto) {
        User user = BeanConvertUtil.convert(dto, User.class);
        return Result.success(userService.register(user));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @PostMapping("/resetPassword")
    @Operation(summary = "重置密码")
    public Result<Boolean> resetPassword(@Valid @RequestBody UserDTO dto) {
        User user = BeanConvertUtil.convert(dto, User.class);
        return Result.success(userService.resetPassword(user));
    }
}