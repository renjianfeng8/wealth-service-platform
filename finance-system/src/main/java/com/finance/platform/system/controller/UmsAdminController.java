package com.finance.platform.system.controller;

import com.finance.common.result.Result;
import com.finance.platform.system.entity.UmsAdmin;
import com.finance.platform.system.service.UmsAdminService;
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
 * 后台管理员表控制器。
 */
@RestController
@Tag(name = "管理员管理", description = "ums_admin 后台管理员相关接口")
@RequestMapping("/umsAdmin")
public class UmsAdminController {

    private final UmsAdminService umsAdminService;

    /**
     * 后台管理员表控制器构造器。
     *
     * @param umsAdminService 后台管理员业务服务
     */
    public UmsAdminController(UmsAdminService umsAdminService) {
        this.umsAdminService = umsAdminService;
    }

    /**
     * 根据 ID 查询后台管理员信息。
     *
     * @param id 后台管理员 ID
     * @return 查询结果
     */
    @Operation(summary = "根据ID查询后台管理员信息")
    @GetMapping("/{id}")
    public Result<UmsAdmin> getById(@PathVariable Long id) {
        return Result.success(umsAdminService.getById(id));
    }

    /**
     * 查询后台管理员列表（不分页）。
     *
     * @return 后台管理员列表
     */
    @Operation(summary = "查询后台管理员列表")
    @GetMapping
    public Result<List<UmsAdmin>> list() {
        return Result.success(umsAdminService.list());
    }

    /**
     * 创建后台管理员。
     *
     * @param umsAdmin 后台管理员入参
     * @return 是否创建成功
     */
    @Operation(summary = "创建后台管理员")
    @PostMapping
    public Result<Boolean> create(@RequestBody UmsAdmin umsAdmin) {
        boolean saved = umsAdminService.save(umsAdmin);
        return Result.success(saved);
    }

    /**
     * 更新后台管理员信息。
     *
     * @param id 后台管理员 ID
     * @param umsAdmin 后台管理员入参
     * @return 是否更新成功
     */
    @Operation(summary = "更新后台管理员信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody UmsAdmin umsAdmin) {
        umsAdmin.setId(id);
        boolean updated = umsAdminService.updateById(umsAdmin);
        return Result.success(updated);
    }

    /**
     * 删除后台管理员（物理删除）。
     *
     * @param id 后台管理员 ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除后台管理员（物理删除）")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = umsAdminService.removeById(id);
        return Result.success(removed);
    }
}

