package com.wealth.platform.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.system.dto.UmsRoleResourceRelationDTO;
import com.wealth.platform.system.entity.UmsRoleResourceRelation;
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

    @Operation(summary = "根据ID查询")
    @GetMapping("/{id}")
    public Result<UmsRoleResourceRelationVO> getById(@PathVariable Long id) {
        return Result.success(umsRoleResourceRelationService.getRoleResourceRelationById(id));
    }

    @Operation(summary = "列表")
    @GetMapping
    public Result<List<UmsRoleResourceRelationVO>> list(
            @Min(1) @RequestParam(defaultValue = "1") Integer pageNum,
            @Min(1) @Max(200) @RequestParam(defaultValue = "20") Integer pageSize) {
        List<UmsRoleResourceRelation> list = umsRoleResourceRelationService.page(new Page<>(pageNum, pageSize)).getRecords();
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
        return Result.success(umsRoleResourceRelationService.createRelation(dto));
    }

    @Operation(summary = "更新")
    @PutMapping("/{id}")
    @AuditLog(module = "系统管理", operation = "更新角色-资源关联")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody UmsRoleResourceRelationDTO dto) {
        return Result.success(umsRoleResourceRelationService.updateRelation(id, dto));
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    @AuditLog(module = "系统管理", operation = "删除角色-资源关联")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(umsRoleResourceRelationService.deleteRelation(id));
    }
}