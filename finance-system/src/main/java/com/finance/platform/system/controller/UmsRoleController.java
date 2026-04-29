package com.finance.platform.system.controller;

import com.finance.common.result.Result;
import com.finance.platform.system.entity.UmsRole;
import com.finance.platform.system.service.UmsRoleService;
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

/**
 * 后台角色表控制器。
 */
@RestController
@Tag(name = "角色管理", description = "ums_role 后台角色相关接口")
@RequestMapping("/umsRole")
public class UmsRoleController {

    private final UmsRoleService umsRoleService;

    /**
     * 后台角色表控制器构造器。
     *
     * @param umsRoleService 后台角色业务服务
     */
    public UmsRoleController(UmsRoleService umsRoleService) {
        this.umsRoleService = umsRoleService;
    }

    /**
     * 根据 ID 查询后台角色信息。
     *
     * @param id 后台角色 ID
     * @return 查询结果
     */
    @Operation(summary = "根据ID查询后台角色信息")
    @GetMapping("/{id}")
    public Result<UmsRole> getById(@PathVariable Long id) {
        return Result.success(umsRoleService.getById(id));
    }

    /**
     * 查询后台角色列表（不分页）。
     *
     * @return 后台角色列表
     */
    @Operation(summary = "查询后台角色列表")
    @GetMapping
    public Result<List<UmsRole>> list() {
        return Result.success(umsRoleService.list());
    }

    /**
     * 创建后台角色。
     *
     * @param umsRole 后台角色入参
     * @return 是否创建成功
     */
    @Operation(summary = "创建后台角色")
    @PostMapping
    public Result<Boolean> create(@RequestBody UmsRole umsRole) {
        boolean saved = umsRoleService.save(umsRole);
        return Result.success(saved);
    }

    /**
     * 更新后台角色信息。
     *
     * @param id 后台角色 ID
     * @param umsRole 后台角色入参
     * @return 是否更新成功
     */
    @Operation(summary = "更新后台角色信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody UmsRole umsRole) {
        umsRole.setId(id);
        boolean updated = umsRoleService.updateById(umsRole);
        return Result.success(updated);
    }

    /**
     * 删除后台角色（物理删除）。
     *
     * @param id 后台角色 ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除后台角色（物理删除）")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = umsRoleService.removeById(id);
        return Result.success(removed);
    }
}

