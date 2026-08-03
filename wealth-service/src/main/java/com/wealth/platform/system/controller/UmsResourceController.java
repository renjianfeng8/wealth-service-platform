package com.wealth.platform.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
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

    @Operation(summary = "查询后台资源列表")
    @GetMapping
    public Result<List<UmsResourceVO>> list(
            @Min(1) @RequestParam(defaultValue = "1") Integer pageNum,
            @Min(1) @Max(200) @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(umsResourceService.getResourceList(pageNum, pageSize));
    }

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

    @Operation(summary = "创建后台资源")
    @PostMapping
    @AuditLog(module = "系统管理", operation = "创建资源")
    @AntiReplay
    public Result<Boolean> create(@Valid @RequestBody UmsResourceDTO dto) {
        return Result.success(umsResourceService.createResource(dto));
    }

    @Operation(summary = "更新后台资源信息")
    @PutMapping("/{id}")
    @AuditLog(module = "系统管理", operation = "更新资源")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody UmsResourceDTO dto) {
        return Result.success(umsResourceService.updateResource(id, dto));
    }

    @Operation(summary = "删除后台资源")
    @DeleteMapping("/{id}")
    @AuditLog(module = "系统管理", operation = "删除资源")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(umsResourceService.deleteResource(id));
    }
}