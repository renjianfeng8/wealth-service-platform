package com.finance.platform.system.controller;

import com.finance.common.result.Result;
import com.finance.platform.system.entity.UmsResource;
import com.finance.platform.system.service.UmsResourceService;
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
 * 后台资源表控制器。
 */
@RestController
@Tag(name = "资源管理", description = "ums_resource 后台资源相关接口")
@RequestMapping("/umsResource")
public class UmsResourceController {

    private final UmsResourceService umsResourceService;

    /**
     * 后台资源表控制器构造器。
     *
     * @param umsResourceService 后台资源业务服务
     */
    public UmsResourceController(UmsResourceService umsResourceService) {
        this.umsResourceService = umsResourceService;
    }

    /**
     * 根据 ID 查询后台资源信息。
     *
     * @param id 后台资源 ID
     * @return 查询结果
     */
    @Operation(summary = "根据ID查询后台资源信息")
    @GetMapping("/{id}")
    public Result<UmsResource> getById(@PathVariable Long id) {
        return Result.success(umsResourceService.getById(id));
    }

    /**
     * 查询后台资源列表（不分页）。
     *
     * @return 后台资源列表
     */
    @Operation(summary = "查询后台资源列表")
    @GetMapping
    public Result<List<UmsResource>> list() {
        return Result.success(umsResourceService.list());
    }

    /**
     * 创建后台资源。
     *
     * @param umsResource 后台资源入参
     * @return 是否创建成功
     */
    @Operation(summary = "创建后台资源")
    @PostMapping
    public Result<Boolean> create(@RequestBody UmsResource umsResource) {
        boolean saved = umsResourceService.save(umsResource);
        return Result.success(saved);
    }

    /**
     * 更新后台资源信息。
     *
     * @param id 后台资源 ID
     * @param umsResource 后台资源入参
     * @return 是否更新成功
     */
    @Operation(summary = "更新后台资源信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody UmsResource umsResource) {
        umsResource.setId(id);
        boolean updated = umsResourceService.updateById(umsResource);
        return Result.success(updated);
    }

    /**
     * 删除后台资源（物理删除）。
     *
     * @param id 后台资源 ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除后台资源（物理删除）")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = umsResourceService.removeById(id);
        return Result.success(removed);
    }
}

