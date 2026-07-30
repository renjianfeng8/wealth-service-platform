package com.wealth.platform.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.system.dto.UmsAdminRoleRelationDTO;
import com.wealth.platform.system.entity.UmsAdminRoleRelation;
import com.wealth.platform.system.service.UmsAdminRoleRelationService;
import com.wealth.platform.system.service.UmsAdminService;
import com.wealth.platform.system.vo.UmsAdminRoleRelationVO;
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
@RequestMapping("/system/umsAdminRoleRelation")
@Tag(name = "管理员角色关联", description = "ums_admin_role_relation")
@Validated
@RequiredArgsConstructor
public class UmsAdminRoleRelationController {

    private final UmsAdminRoleRelationService umsAdminRoleRelationService;
    private final UmsAdminService umsAdminService;

    @Operation(summary = "根据ID查询")
    @GetMapping("/{id}")
    public Result<UmsAdminRoleRelationVO> getById(@PathVariable Long id) {
        UmsAdminRoleRelation relation = umsAdminRoleRelationService.getById(id);
        if (relation == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(BeanConvertUtil.convert(relation, UmsAdminRoleRelationVO.class));
    }

    @Operation(summary = "列表")
    @GetMapping
    public Result<List<UmsAdminRoleRelationVO>> list(
            @Min(1) @RequestParam(defaultValue = "1") Integer pageNum,
            @Min(1) @Max(200) @RequestParam(defaultValue = "20") Integer pageSize) {
        List<UmsAdminRoleRelation> list = umsAdminRoleRelationService.page(new Page<>(pageNum, pageSize)).getRecords();
        return Result.success(BeanConvertUtil.convertList(list, UmsAdminRoleRelationVO.class));
    }

    @Operation(summary = "分页")
    @GetMapping("/page")
    public Result<IPage<UmsAdminRoleRelationVO>> page(
            @Min(1) @RequestParam(defaultValue = "1") Integer pageNum,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long adminId) {
        IPage<UmsAdminRoleRelation> page = umsAdminRoleRelationService.pageWithFilter(pageNum, pageSize, adminId);
        return Result.success(BeanConvertUtil.convertPage(page, UmsAdminRoleRelationVO.class));
    }

    @Operation(summary = "创建")
    @PostMapping
    @AuditLog(module = "系统管理", operation = "创建管理员-角色关联")
    @AntiReplay
    public Result<Boolean> create(@Valid @RequestBody UmsAdminRoleRelationDTO dto) {
        UmsAdminRoleRelation relation = BeanConvertUtil.convert(dto, UmsAdminRoleRelation.class);
        boolean saved = umsAdminRoleRelationService.save(relation);
        // 清除该管理员的权限缓存，使新角色立即生效
        umsAdminService.clearPermissionCache(dto.getAdminId());
        return Result.success(saved);
    }

    @Operation(summary = "更新")
    @PutMapping("/{id}")
    @AuditLog(module = "系统管理", operation = "更新管理员-角色关联")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody UmsAdminRoleRelationDTO dto) {
        UmsAdminRoleRelation existing = umsAdminRoleRelationService.getById(id);
        if (existing == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        BeanConvertUtil.copyNonNullProperties(dto, existing);
        existing.setId(id);
        boolean updated = umsAdminRoleRelationService.updateById(existing);
        // 清除新旧管理员的权限缓存
        umsAdminService.clearPermissionCache(existing.getAdminId());
        if (dto.getAdminId() != null && !dto.getAdminId().equals(existing.getAdminId())) {
            umsAdminService.clearPermissionCache(dto.getAdminId());
        }
        return Result.success(updated);
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    @AuditLog(module = "系统管理", operation = "删除管理员-角色关联")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        UmsAdminRoleRelation existing = umsAdminRoleRelationService.getById(id);
        if (existing == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        boolean removed = umsAdminRoleRelationService.removeById(id);
        // 清除该管理员的权限缓存，使角色移除立即生效
        umsAdminService.clearPermissionCache(existing.getAdminId());
        return Result.success(removed);
    }
}