package com.finance.platform.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.common.result.Result;
import com.finance.common.result.ResultCode;
import com.finance.common.utils.BeanConvertUtil;
import com.finance.platform.system.dto.UmsResourceDTO;
import com.finance.platform.system.entity.UmsResource;
import com.finance.platform.system.service.UmsResourceService;
import com.finance.platform.system.vo.UmsResourceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台资源表控制器。
 */
@RestController
@Tag(name = "资源管理", description = "ums_resource 后台资源相关接口")
@RequestMapping("/umsResource")
public class UmsResourceController {

    private final UmsResourceService umsResourceService;

    /**
     * 后台资源表控制器构造器。
     *
     * @param umsResourceService 后台资源业务服务
     */
    public UmsResourceController(UmsResourceService umsResourceService) {
        this.umsResourceService = umsResourceService;
    }

    /**
     * 根据 ID 查询后台资源信息。
     *
     * @param id 后台资源 ID
     * @return 查询结果
     */
    @Operation(summary = "根据ID查询后台资源信息")
    @GetMapping("/{id}")
    public Result<UmsResourceVO> getById(@PathVariable Long id) {
        UmsResource resource = umsResourceService.getById(id);
        if (resource == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(BeanConvertUtil.convert(resource, UmsResourceVO.class));
    }

    /**
     * 查询后台资源列表（不分页）。
     *
     * @return 后台资源列表
     */
    @Operation(summary = "查询后台资源列表")
    @GetMapping
    public Result<List<UmsResourceVO>> list() {
        List<UmsResource> list = umsResourceService.list();
        return Result.success(BeanConvertUtil.convertList(list, UmsResourceVO.class));
    }

    /**
     * 分页查询后台资源列表。
     *
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @Operation(summary = "分页查询后台资源列表")
    @GetMapping("/page")
    public Result<IPage<UmsResourceVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<UmsResource> page = new Page<>(pageNum, pageSize);
        IPage<UmsResource> resourcePage = umsResourceService.page(page);

        Page<UmsResourceVO> voPage = new Page<>();
        voPage.setCurrent(resourcePage.getCurrent());
        voPage.setSize(resourcePage.getSize());
        voPage.setTotal(resourcePage.getTotal());
        voPage.setPages(resourcePage.getPages());
        voPage.setRecords(BeanConvertUtil.convertList(resourcePage.getRecords(), UmsResourceVO.class));

        return Result.success(voPage);
    }

    /**
     * 创建后台资源。
     *
     * @param dto 后台资源入参
     * @return 是否创建成功
     */
    @Operation(summary = "创建后台资源")
    @PostMapping
    public Result<Boolean> create(@RequestBody UmsResourceDTO dto) {
        UmsResource resource = BeanConvertUtil.convert(dto, UmsResource.class);
        boolean saved = umsResourceService.save(resource);
        return Result.success(saved);
    }

    /**
     * 更新后台资源信息。
     *
     * @param id 后台资源 ID
     * @param dto 后台资源入参
     * @return 是否更新成功
     */
    @Operation(summary = "更新后台资源信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody UmsResourceDTO dto) {
        UmsResource resource = BeanConvertUtil.convert(dto, UmsResource.class);
        resource.setId(id);
        boolean updated = umsResourceService.updateById(resource);
        return Result.success(updated);
    }

    /**
     * 删除后台资源（逻辑删除）。
     *
     * @param id 后台资源 ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除后台资源")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = umsResourceService.removeById(id);
        return Result.success(removed);
    }
}