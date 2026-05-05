package com.finance.platform.search.controller;

import com.finance.common.result.Result;
import com.finance.platform.search.entity.ProductDocument;
import com.finance.platform.search.service.ProductSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search/product")
@Tag(name = "产品搜索接口")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    @PostMapping
    @Operation(summary = "新增/更新产品到ES")
    public Result<ProductDocument> save(@RequestBody ProductDocument document) {
        return Result.success(productSearchService.save(document));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询ES产品")
    public Result<ProductDocument> getById(@PathVariable Long id) {
        return Result.success(productSearchService.getById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "关键词搜索产品")
    public Result<Page<ProductDocument>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(productSearchService.search(keyword, page, size));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除ES产品")
    public Result<Void> delete(@PathVariable Long id) {
        productSearchService.deleteById(id);
        return Result.success();
    }
}