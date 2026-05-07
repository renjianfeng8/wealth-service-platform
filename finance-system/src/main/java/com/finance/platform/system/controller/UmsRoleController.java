package com.finance.platform.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.common.result.Result;
import com.finance.common.result.ResultCode;
import com.finance.common.utils.BeanConvertUtil;
import com.finance.platform.system.dto.UmsRoleDTO;
import com.finance.platform.system.entity.UmsRole;
import com.finance.platform.system.service.UmsRoleService;
import com.finance.platform.system.vo.UmsRoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "角色管理", description = "ums_role 后台角色相关接口")
@RequestMapping("/umsRole")
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
        List<UmsRole> list = umsRoleService.list();
        return Result.success(BeanConvertUtil.convertList(list, UmsRoleVO.class));
    }

    @Operation(summary = "分页查询角色列表")
    @GetMapping("/page")
    public Result<IPage<UmsRoleVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<UmsRole> page = new Page<>(pageNum, pageSize);
        IPage<UmsRole> rolePage = umsRoleService.page(page);

        Page<UmsRoleVO> voPage = new Page<>();
        voPage.setCurrent(rolePage.getCurrent());
        voPage.setSize(rolePage.getSize());
        voPage.setTotal(rolePage.getTotal());
        voPage.setPages(rolePage.getPages());
        voPage.setRecords(BeanConvertUtil.convertList(rolePage.getRecords(), UmsRoleVO.class));

        return Result.success(voPage);
    }

    @Operation(summary = "创建角色")
    @PostMapping
    public Result<Boolean> create(@RequestBody UmsRoleDTO dto) {
        UmsRole role = BeanConvertUtil.convert(dto, UmsRole.class);
        return Result.success(umsRoleService.save(role));
    }

    @Operation(summary = "更新角色信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody UmsRoleDTO dto) {
        UmsRole role = BeanConvertUtil.convert(dto, UmsRole.class);
        role.setId(id);
        return Result.success(umsRoleService.updateById(role));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(umsRoleService.removeById(id));
    }
}