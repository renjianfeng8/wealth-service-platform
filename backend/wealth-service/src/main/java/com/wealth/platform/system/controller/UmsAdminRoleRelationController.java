package com.wealth.platform.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.system.dto.UmsAdminRoleRelationDTO;
import com.wealth.platform.system.entity.UmsAdminRoleRelation;
import com.wealth.platform.system.service.UmsAdminRoleRelationService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/umsAdminRoleRelation")
@Tag(name = "管理员角色关联", description = "ums_admin_role_relation")
@Validated
@RequiredArgsConstructor
public class UmsAdminRoleRelationController {

    private final UmsAdminRoleRelationService umsAdminRoleRelationService;

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
        return Result.success(umsAdminRoleRelationService.createRelation(dto));
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    @AuditLog(module = "系统管理", operation = "删除管理员-角色关联")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(umsAdminRoleRelationService.deleteRelation(id));
    }
}