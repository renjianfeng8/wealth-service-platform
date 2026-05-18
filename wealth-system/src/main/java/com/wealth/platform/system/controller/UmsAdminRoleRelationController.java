package com.wealth.platform.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.system.dto.UmsAdminRoleRelationDTO;
import com.wealth.platform.system.entity.UmsAdminRoleRelation;
import com.wealth.platform.system.service.UmsAdminRoleRelationService;
import com.wealth.platform.system.vo.UmsAdminRoleRelationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/umsAdminRoleRelation")
@Tag(name = "管理员角色关联", description = "ums_admin_role_relation")
public class UmsAdminRoleRelationController {

    private final UmsAdminRoleRelationService umsAdminRoleRelationService;

    public UmsAdminRoleRelationController(UmsAdminRoleRelationService umsAdminRoleRelationService) {
        this.umsAdminRoleRelationService = umsAdminRoleRelationService;
    }

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
    public Result<List<UmsAdminRoleRelationVO>> list() {
        List<UmsAdminRoleRelation> list = umsAdminRoleRelationService.list();
        return Result.success(BeanConvertUtil.convertList(list, UmsAdminRoleRelationVO.class));
    }

    @Operation(summary = "分页")
    @GetMapping("/page")
    public Result<IPage<UmsAdminRoleRelationVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<UmsAdminRoleRelation> page = new Page<>(pageNum, pageSize);
        IPage<UmsAdminRoleRelation> pageResult = umsAdminRoleRelationService.page(page);

        Page<UmsAdminRoleRelationVO> voPage = new Page<>();
        voPage.setCurrent(pageResult.getCurrent());
        voPage.setSize(pageResult.getSize());
        voPage.setTotal(pageResult.getTotal());
        voPage.setPages(pageResult.getPages());
        voPage.setRecords(BeanConvertUtil.convertList(pageResult.getRecords(), UmsAdminRoleRelationVO.class));
        return Result.success(voPage);
    }

    @Operation(summary = "创建")
    @PostMapping
    public Result<Boolean> create(@Valid @RequestBody UmsAdminRoleRelationDTO dto) {
        UmsAdminRoleRelation relation = BeanConvertUtil.convert(dto, UmsAdminRoleRelation.class);
        return Result.success(umsAdminRoleRelationService.save(relation));
    }

    @Operation(summary = "更新")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody UmsAdminRoleRelationDTO dto) {
        UmsAdminRoleRelation relation = BeanConvertUtil.convert(dto, UmsAdminRoleRelation.class);
        relation.setId(id);
        return Result.success(umsAdminRoleRelationService.updateById(relation));
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(umsAdminRoleRelationService.removeById(id));
    }
}