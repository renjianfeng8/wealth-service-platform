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
import com.wealth.platform.system.service.UmsRoleResourceRelationService;
import com.wealth.platform.system.vo.UmsRoleResourceRelationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/umsRoleResourceRelation")
@Tag(name = "角色-资源关联", description = "ums_role_resource_relation")
@Validated
public class UmsRoleResourceRelationController {

    private final UmsRoleResourceRelationService umsRoleResourceRelationService;

    public UmsRoleResourceRelationController(UmsRoleResourceRelationService umsRoleResourceRelationService) {
        this.umsRoleResourceRelationService = umsRoleResourceRelationService;
    }

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
        List<UmsRoleResourceRelation> list = umsRoleResourceRelationService.lambdaQuery().last("LIMIT 1000").list();
        return Result.success(BeanConvertUtil.convertList(list, UmsRoleResourceRelationVO.class));
    }

    @Operation(summary = "分页")
    @GetMapping("/page")
    public Result<IPage<UmsRoleResourceRelationVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<UmsRoleResourceRelation> page = new Page<>(pageNum, pageSize);
        return Result.success(BeanConvertUtil.convertPage(umsRoleResourceRelationService.page(page), UmsRoleResourceRelationVO.class));
    }

    @Operation(summary = "创建")
    @PostMapping
    @AuditLog(module = "系统管理", operation = "创建角色-资源关联")
    @AntiReplay
    public Result<Boolean> create(@Valid @RequestBody UmsRoleResourceRelationDTO dto) {
        UmsRoleResourceRelation relation = BeanConvertUtil.convert(dto, UmsRoleResourceRelation.class);
        return Result.success(umsRoleResourceRelationService.save(relation));
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
        return Result.success(umsRoleResourceRelationService.updateById(existing));
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
        return Result.success(umsRoleResourceRelationService.removeById(id));
    }
}