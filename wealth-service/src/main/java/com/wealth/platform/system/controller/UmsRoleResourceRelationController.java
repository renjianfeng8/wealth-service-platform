package com.wealth.platform.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.system.dto.UmsRoleResourceRelationDTO;
import com.wealth.platform.system.entity.UmsRoleResourceRelation;
import com.wealth.platform.system.service.UmsAdminRoleRelationService;
import com.wealth.platform.system.service.UmsAdminService;
import com.wealth.platform.system.service.UmsRoleResourceRelationService;
import com.wealth.platform.system.vo.UmsRoleResourceRelationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/system/umsRoleResourceRelation")
@Tag(name = "角色-资源关联", description = "ums_role_resource_relation")
@Validated
@RequiredArgsConstructor
public class UmsRoleResourceRelationController {

    private final UmsRoleResourceRelationService umsRoleResourceRelationService;
    private final UmsAdminService umsAdminService;
    private final UmsAdminRoleRelationService umsAdminRoleRelationService;

    @Operation(summary = "根据ID查询")
    @GetMapping("/{id}")
    public Result<UmsRoleResourceRelationVO> getById(@PathVariable Long id) {
        UmsRoleResourceRelation relation = umsRoleResourceRelationService.getById(id);
        if (relation == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(BeanConvertUtil.convert(relation, UmsRoleResourceRelationVO.class));
    }

    @Operation(summary = "列表")
    @GetMapping
    public Result<List<UmsRoleResourceRelationVO>> list() {
        List<UmsRoleResourceRelation> list = umsRoleResourceRelationService.page(new Page<>(1, 1000)).getRecords();
        return Result.success(BeanConvertUtil.convertList(list, UmsRoleResourceRelationVO.class));
    }

    @Operation(summary = "分页")
    @GetMapping("/page")
    public Result<IPage<UmsRoleResourceRelationVO>> page(
            @Min(1) @RequestParam(defaultValue = "1") Integer pageNum,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long roleId) {
        IPage<UmsRoleResourceRelation> page = umsRoleResourceRelationService.pageWithFilter(pageNum, pageSize, roleId);
        return Result.success(BeanConvertUtil.convertPage(page, UmsRoleResourceRelationVO.class));
    }

    @Operation(summary = "创建")
    @PostMapping
    @AuditLog(module = "系统管理", operation = "创建角色-资源关联")
    @AntiReplay
    public Result<Boolean> create(@Valid @RequestBody UmsRoleResourceRelationDTO dto) {
        UmsRoleResourceRelation relation = BeanConvertUtil.convert(dto, UmsRoleResourceRelation.class);
        boolean saved = umsRoleResourceRelationService.save(relation);
        clearCacheByRoleId(dto.getRoleId());
        return Result.success(saved);
    }

    @Operation(summary = "更新")
    @PutMapping("/{id}")
    @AuditLog(module = "系统管理", operation = "更新角色-资源关联")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody UmsRoleResourceRelationDTO dto) {
        UmsRoleResourceRelation existing = umsRoleResourceRelationService.getById(id);
        if (existing == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        BeanConvertUtil.copyNonNullProperties(dto, existing);
        existing.setId(id);
        boolean updated = umsRoleResourceRelationService.updateById(existing);
        // 清除新旧角色的权限缓存
        clearCacheByRoleId(existing.getRoleId());
        if (dto.getRoleId() != null && !dto.getRoleId().equals(existing.getRoleId())) {
            clearCacheByRoleId(dto.getRoleId());
        }
        return Result.success(updated);
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    @AuditLog(module = "系统管理", operation = "删除角色-资源关联")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        UmsRoleResourceRelation existing = umsRoleResourceRelationService.getById(id);
        if (existing == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        boolean removed = umsRoleResourceRelationService.removeById(id);
        clearCacheByRoleId(existing.getRoleId());
        return Result.success(removed);
    }

    /** 清除拥有指定角色的所有管理员的权限缓存 */
    private void clearCacheByRoleId(Long roleId) {
        if (roleId == null) return;
        List<Long> adminIds = umsAdminRoleRelationService.getAdminIdByRoleId(roleId);
        for (Long adminId : adminIds) {
            umsAdminService.clearPermissionCache(adminId);
        }
    }
}