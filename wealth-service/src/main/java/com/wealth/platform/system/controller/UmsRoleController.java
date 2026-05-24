package com.wealth.platform.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.system.dto.UmsRoleDTO;
import com.wealth.platform.system.entity.UmsRole;
import com.wealth.platform.system.service.UmsRoleService;
import com.wealth.platform.system.vo.UmsRoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "角色管理", description = "ums_role 后台角色相关接口")
@RequestMapping("/system/umsRole")
@Validated
public class UmsRoleController {

    private final UmsRoleService umsRoleService;

    public UmsRoleController(UmsRoleService umsRoleService) {
        this.umsRoleService = umsRoleService;
    }

    @Operation(summary = "根据ID查询角色信息")
    @GetMapping("/{id}")
    public Result<UmsRoleVO> getById(@PathVariable Long id) {
        UmsRole role = umsRoleService.getById(id);
        if (role == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(BeanConvertUtil.convert(role, UmsRoleVO.class));
    }

    @Operation(summary = "查询角色列表")
    @GetMapping
    public Result<List<UmsRoleVO>> list() {
        List<UmsRole> list = umsRoleService.page(new Page<>(1, 1000)).getRecords();
        return Result.success(BeanConvertUtil.convertList(list, UmsRoleVO.class));
    }

    @Operation(summary = "分页查询角色列表")
    @GetMapping("/page")
    public Result<IPage<UmsRoleVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<UmsRole> page = new Page<>(pageNum, pageSize);
        return Result.success(BeanConvertUtil.convertPage(umsRoleService.page(page), UmsRoleVO.class));
    }

    @PostMapping
    @Operation(summary = "创建角色")
    @AuditLog(module = "系统管理", operation = "创建角色")
    @AntiReplay
    public Result<Boolean> create(@Valid @RequestBody UmsRoleDTO dto) {
        UmsRole role = BeanConvertUtil.convert(dto, UmsRole.class);
        return Result.success(umsRoleService.save(role));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色信息")
    @AuditLog(module = "系统管理", operation = "更新角色")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody UmsRoleDTO dto) {
        UmsRole existing = umsRoleService.getById(id);
        if (existing == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        BeanConvertUtil.copyNonNullProperties(dto, existing);
        existing.setId(id);
        return Result.success(umsRoleService.updateById(existing));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    @AuditLog(module = "系统管理", operation = "删除角色")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        UmsRole existing = umsRoleService.getById(id);
        if (existing == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(umsRoleService.removeById(id));
    }
}