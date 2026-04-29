package com.finance.platform.system.controller;

import com.finance.common.result.Result;
import com.finance.platform.system.entity.UmsRoleResourceRelation;
import com.finance.platform.system.service.UmsRoleResourceRelationService;
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
 * 后台角色资源关系表控制器。
 */
@RestController
@Tag(name = "角色资源关系管理", description = "ums_role_resource_relation 后台角色资源关系相关接口")
@RequestMapping("/umsRoleResourceRelation")
public class UmsRoleResourceRelationController {

    private final UmsRoleResourceRelationService umsRoleResourceRelationService;

    /**
     * 后台角色资源关系表控制器构造器。
     *
     * @param umsRoleResourceRelationService 后台角色资源关系业务服务
     */
    public UmsRoleResourceRelationController(UmsRoleResourceRelationService umsRoleResourceRelationService) {
        this.umsRoleResourceRelationService = umsRoleResourceRelationService;
    }

    /**
     * 根据 ID 查询后台角色资源关系列表信息。
     *
     * @param id 后台角色资源关系 ID
     * @return 查询结果
     */
    @Operation(summary = "根据ID查询后台角色资源关系")
    @GetMapping("/{id}")
    public Result<UmsRoleResourceRelation> getById(@PathVariable Long id) {
        return Result.success(umsRoleResourceRelationService.getById(id));
    }

    /**
     * 查询后台角色资源关系列表（不分页）。
     *
     * @return 后台角色资源关系列表
     */
    @Operation(summary = "查询后台角色资源关系列表")
    @GetMapping
    public Result<List<UmsRoleResourceRelation>> list() {
        return Result.success(umsRoleResourceRelationService.list());
    }

    /**
     * 创建后台角色资源关系。
     *
     * @param umsRoleResourceRelation 后台角色资源关系入参
     * @return 是否创建成功
     */
    @Operation(summary = "创建后台角色资源关系")
    @PostMapping
    public Result<Boolean> create(@RequestBody UmsRoleResourceRelation umsRoleResourceRelation) {
        boolean saved = umsRoleResourceRelationService.save(umsRoleResourceRelation);
        return Result.success(saved);
    }

    /**
     * 更新后台角色资源关系列表信息。
     *
     * @param id 后台角色资源关系 ID
     * @param umsRoleResourceRelation 后台角色资源关系入参
     * @return 是否更新成功
     */
    @Operation(summary = "更新后台角色资源关系列表信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody UmsRoleResourceRelation umsRoleResourceRelation) {
        umsRoleResourceRelation.setId(id);
        boolean updated = umsRoleResourceRelationService.updateById(umsRoleResourceRelation);
        return Result.success(updated);
    }

    /**
     * 删除后台角色资源关系（物理删除）。
     *
     * @param id 后台角色资源关系 ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除后台角色资源关系（物理删除）")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = umsRoleResourceRelationService.removeById(id);
        return Result.success(removed);
    }
}

