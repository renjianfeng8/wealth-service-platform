package com.wealth.platform.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.system.dto.UmsRoleDTO;
import com.wealth.platform.system.entity.UmsRole;
import com.wealth.platform.system.service.UmsRoleService;
import com.wealth.platform.system.vo.UmsRoleVO;
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
@Tag(name = "角色管理", description = "ums_role 后台角色相关接口")
@RequestMapping("/system/umsRole")
@Validated
@RequiredArgsConstructor
public class UmsRoleController {

    private final UmsRoleService umsRoleService;

    @Operation(summary = "根据ID查询角色信息")
    @GetMapping("/{id}")
    public Result<UmsRoleVO> getById(@PathVariable Long id) {
        return Result.success(umsRoleService.getRoleById(id));
    }

    @Operation(summary = "查询角色列表")
    @GetMapping
    public Result<List<UmsRoleVO>> list(
            @Min(1) @RequestParam(defaultValue = "1") Integer pageNum,
            @Min(1) @Max(200) @RequestParam(defaultValue = "20") Integer pageSize) {
        List<UmsRole> list = umsRoleService.page(new Page<>(pageNum, pageSize)).getRecords();
        return Result.success(BeanConvertUtil.convertList(list, UmsRoleVO.class));
    }

    @Operation(summary = "分页查询角色列表")
    @GetMapping("/page")
    public Result<IPage<UmsRoleVO>> page(
            @Min(1) @RequestParam(defaultValue = "1") Integer pageNum,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {

        IPage<UmsRole> page = umsRoleService.pageWithFilter(pageNum, pageSize, name, status);
        return Result.success(BeanConvertUtil.convertPage(page, UmsRoleVO.class));
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
        return Result.success(umsRoleService.updateRole(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    @AuditLog(module = "系统管理", operation = "删除角色")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(umsRoleService.deleteRole(id));
    }
}