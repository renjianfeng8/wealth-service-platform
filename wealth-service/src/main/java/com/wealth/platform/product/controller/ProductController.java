package com.wealth.platform.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.dto.ProductSyncDTO;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.platform.product.dto.FinProductDTO;
import com.wealth.platform.product.entity.WeaProduct;
import com.wealth.platform.product.service.FinProductService;
import com.wealth.platform.product.service.ProductSyncService;
import com.wealth.platform.product.vo.FinProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "产品管理", description = "产品相关接口")
@RequestMapping("/product/wea-product")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final FinProductService finProductService;
    private final ProductSyncService productSyncService;

    @Operation(summary = "根据ID查询产品")
    @GetMapping("/{id}")
    public Result<FinProductVO> getById(@PathVariable Long id) {
        FinProductVO vo = finProductService.getProductById(id);
        if (vo == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(vo);
    }

    @Operation(summary = "查询产品列表")
    @GetMapping
    public Result<List<FinProductVO>> list() {
        return Result.success(finProductService.getProductList());
    }

    @Operation(summary = "分页查询产品")
    @GetMapping("/page")
    public Result<IPage<FinProductVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) Integer productType) {
        Page<WeaProduct> page = new Page<>(pageNum, pageSize);
        return Result.success(finProductService.pageProducts(page, productName, productCode, productType));
    }

    @Operation(summary = "创建产品")
    @PostMapping
    @AuditLog(module = "产品管理", operation = "创建产品")
    @AntiReplay
    public Result<Boolean> create(@Valid @RequestBody FinProductDTO dto) {
        return Result.success(finProductService.createProduct(dto));
    }

    @Operation(summary = "更新产品信息")
    @PutMapping("/{id}")
    @AuditLog(module = "产品管理", operation = "更新产品")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody FinProductDTO dto) {
        return Result.success(finProductService.updateProduct(id, dto));
    }

    @Operation(summary = "删除产品")
    @DeleteMapping("/{id}")
    @AuditLog(module = "产品管理", operation = "删除产品")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(finProductService.deleteProduct(id));
    }

    @Operation(summary = "手动同步产品数据到 ES")
    @PostMapping("/syncES")
    @AuditLog(module = "产品管理", operation = "同步产品到ES")
    public Result<List<ProductSyncDTO>> syncToES() {
        return Result.success(productSyncService.syncAllToES());
    }
}