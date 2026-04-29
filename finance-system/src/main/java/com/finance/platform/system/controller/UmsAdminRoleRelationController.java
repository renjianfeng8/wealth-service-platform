package com.finance.platform.system.controller;

import com.finance.common.result.Result;
import com.finance.platform.system.entity.UmsAdminRoleRelation;
import com.finance.platform.system.service.UmsAdminRoleRelationService;
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
 * 后台用户和角色关系表控制器。
 */
@RestController
@Tag(name = "管理员角色关系管理", description = "ums_admin_role_relation 后台管理员角色关系相关接口")
@RequestMapping("/umsAdminRoleRelation")
public class UmsAdminRoleRelationController {

    private final UmsAdminRoleRelationService umsAdminRoleRelationService;

    /**
     * 后台用户和角色关系表控制器构造器。
     *
     * @param umsAdminRoleRelationService 后台管理员角色关系业务服务
     */
    public UmsAdminRoleRelationController(UmsAdminRoleRelationService umsAdminRoleRelationService) {
        this.umsAdminRoleRelationService = umsAdminRoleRelationService;
    }

    /**
     * 根据 ID 查询后台管理员角色关系列表信息。
     *
     * @param id 后台管理员角色关系 ID
     * @return 查询结果
     */
    @Operation(summary = "根据ID查询后台管理员角色关系")
    @GetMapping("/{id}")
    public Result<UmsAdminRoleRelation> getById(@PathVariable Long id) {
        return Result.success(umsAdminRoleRelationService.getById(id));
    }

    /**
     * 查询后台管理员角色关系列表（不分页）。
     *
     * @return 后台管理员角色关系列表
     */
    @Operation(summary = "查询后台管理员角色关系列表")
    @GetMapping
    public Result<List<UmsAdminRoleRelation>> list() {
        return Result.success(umsAdminRoleRelationService.list());
    }

    /**
     * 创建后台管理员角色关系。
     *
     * @param umsAdminRoleRelation 后台管理员角色关系入参
     * @return 是否创建成功
     */
    @Operation(summary = "创建后台管理员角色关系")
    @PostMapping
    public Result<Boolean> create(@RequestBody UmsAdminRoleRelation umsAdminRoleRelation) {
        boolean saved = umsAdminRoleRelationService.save(umsAdminRoleRelation);
        return Result.success(saved);
    }

    /**
     * 更新后台管理员角色关系信息。
     *
     * @param id 后台管理员角色关系 ID
     * @param umsAdminRoleRelation 后台管理员角色关系入参
     * @return 是否更新成功
     */
    @Operation(summary = "更新后台管理员角色关系信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody UmsAdminRoleRelation umsAdminRoleRelation) {
        umsAdminRoleRelation.setId(id);
        boolean updated = umsAdminRoleRelationService.updateById(umsAdminRoleRelation);
        return Result.success(updated);
    }

    /**
     * 删除后台管理员角色关系（物理删除）。
     *
     * @param id 后台管理员角色关系 ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除后台管理员角色关系（物理删除）")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = umsAdminRoleRelationService.removeById(id);
        return Result.success(removed);
    }
}

