package com.wealth.platform.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.system.dto.UmsResourceDTO;
import com.wealth.platform.system.entity.UmsResource;
import com.wealth.platform.system.service.UmsResourceService;
import com.wealth.platform.system.vo.UmsResourceVO;
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

/**
 * 后台资源表控制器。
 */
@RestController
@Tag(name = "资源管理", description = "ums_resource 后台资源相关接口")
@RequestMapping("/system/umsResource")
@Validated
@RequiredArgsConstructor
public class UmsResourceController {

    private final UmsResourceService umsResourceService;

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
        List<UmsResource> list = umsResourceService.page(new Page<>(1, 1000)).getRecords();
        return Result.success(BeanConvertUtil.convertList(list, UmsResourceVO.class));
    }

    /**
     * 分页查询后台资源列表。
     *
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param name    资源名称（可选）
     * @param url     资源路径（可选）
     * @return 分页结果
     */
    @Operation(summary = "分页查询后台资源列表")
    @GetMapping("/page")
    public Result<IPage<UmsResourceVO>> page(
            @Min(1) @RequestParam(defaultValue = "1") Integer pageNum,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String url) {
        IPage<UmsResource> page = umsResourceService.pageWithFilter(pageNum, pageSize, name, url);
        return Result.success(BeanConvertUtil.convertPage(page, UmsResourceVO.class));
    }

    /**
     * 创建后台资源。
     *
     * @param dto 后台资源入参
     * @return 是否创建成功
     */
    @Operation(summary = "创建后台资源")
    @PostMapping
    @AuditLog(module = "系统管理", operation = "创建资源")
    @AntiReplay
    public Result<Boolean> create(@Valid @RequestBody UmsResourceDTO dto) {
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
    @AuditLog(module = "系统管理", operation = "更新资源")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody UmsResourceDTO dto) {
        UmsResource existing = umsResourceService.getById(id);
        if (existing == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        BeanConvertUtil.copyNonNullProperties(dto, existing);
        existing.setId(id);
        return Result.success(umsResourceService.updateById(existing));
    }

    /**
     * 删除后台资源（逻辑删除）。
     *
     * @param id 后台资源 ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除后台资源")
    @DeleteMapping("/{id}")
    @AuditLog(module = "系统管理", operation = "删除资源")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        UmsResource existing = umsResourceService.getById(id);
        if (existing == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        boolean removed = umsResourceService.removeById(id);
        return Result.success(removed);
    }
}