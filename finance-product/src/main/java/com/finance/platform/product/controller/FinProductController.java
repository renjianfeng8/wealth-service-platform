package com.finance.platform.product.controller;

import com.finance.common.result.Result;
import com.finance.platform.product.entity.FinProduct;
import com.finance.platform.product.service.FinProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 产品表控制器。
 */
@RestController
@Tag(name = "产品管理", description = "fin_product 产品相关接口")
@RequestMapping("/finProduct")
public class FinProductController {

    private final FinProductService finProductService;

    /**
     * 产品表控制器构造器。
     *
     * @param finProductService 产品业务服务
     */
    public FinProductController(FinProductService finProductService) {
        this.finProductService = finProductService;
    }

    /**
     * 根据 ID 查询产品信息。
     *
     * @param id 产品 ID
     * @return 查询结果
     */
    @Operation(summary = "根据ID查询产品")
    @GetMapping("/{id}")
    public Result<FinProduct> getById(@PathVariable Long id) {
        return Result.success(finProductService.getById(id));
    }

    /**
     * 查询产品列表（不分页）。
     *
     * @return 产品列表
     */
    @Operation(summary = "查询产品列表")
    @GetMapping
    public Result<List<FinProduct>> list() {
        return Result.success(finProductService.list());
    }

    /**
     * 创建产品。
     *
     * @param finProduct 产品入参
     * @return 是否创建成功
     */
    @Operation(summary = "创建产品")
    @PostMapping
    public Result<Boolean> create(@RequestBody FinProduct finProduct) {
        boolean saved = finProductService.save(finProduct);
        return Result.success(saved);
    }

    /**
     * 更新产品信息。
     *
     * @param id 产品 ID
     * @param finProduct 产品入参
     * @return 是否更新成功
     */
    @Operation(summary = "更新产品信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody FinProduct finProduct) {
        finProduct.setId(id);
        boolean updated = finProductService.updateById(finProduct);
        return Result.success(updated);
    }

    /**
     * 删除产品（逻辑删除）。
     *
     * @param id 产品 ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除产品（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = finProductService.removeById(id);
        return Result.success(removed);
    }
}

