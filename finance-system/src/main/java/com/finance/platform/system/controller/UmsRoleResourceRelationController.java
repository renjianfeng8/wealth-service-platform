package com.finance.platform.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.common.result.Result;
import com.finance.common.result.ResultCode;
import com.finance.common.utils.BeanConvertUtil;
import com.finance.platform.system.dto.UmsRoleResourceRelationDTO;
import com.finance.platform.system.entity.UmsRoleResourceRelation;
import com.finance.platform.system.service.UmsRoleResourceRelationService;
import com.finance.platform.system.vo.UmsRoleResourceRelationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/umsRoleResourceRelation")
@Tag(name = "角色-资源关联", description = "ums_role_resource_relation")
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
        List<UmsRoleResourceRelation> list = umsRoleResourceRelationService.list();
        return Result.success(BeanConvertUtil.convertList(list, UmsRoleResourceRelationVO.class));
    }

    @Operation(summary = "分页")
    @GetMapping("/page")
    public Result<IPage<UmsRoleResourceRelationVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<UmsRoleResourceRelation> page = new Page<>(pageNum, pageSize);
        IPage<UmsRoleResourceRelation> pageResult = umsRoleResourceRelationService.page(page);

        Page<UmsRoleResourceRelationVO> voPage = new Page<>();
        voPage.setCurrent(pageResult.getCurrent());
        voPage.setSize(pageResult.getSize());
        voPage.setTotal(pageResult.getTotal());
        voPage.setPages(pageResult.getPages());
        voPage.setRecords(BeanConvertUtil.convertList(pageResult.getRecords(), UmsRoleResourceRelationVO.class));
        return Result.success(voPage);
    }

    @Operation(summary = "创建")
    @PostMapping
    public Result<Boolean> create(@RequestBody UmsRoleResourceRelationDTO dto) {
        UmsRoleResourceRelation relation = BeanConvertUtil.convert(dto, UmsRoleResourceRelation.class);
        return Result.success(umsRoleResourceRelationService.save(relation));
    }

    @Operation(summary = "更新")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody UmsRoleResourceRelationDTO dto) {
        UmsRoleResourceRelation relation = BeanConvertUtil.convert(dto, UmsRoleResourceRelation.class);
        relation.setId(id);
        return Result.success(umsRoleResourceRelationService.updateById(relation));
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(umsRoleResourceRelationService.removeById(id));
    }
}