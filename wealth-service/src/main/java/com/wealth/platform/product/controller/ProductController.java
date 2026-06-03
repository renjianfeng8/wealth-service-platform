package com.wealth.platform.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.platform.product.dto.ProductDTO;
import com.wealth.platform.product.entity.WeaProduct;
import com.wealth.platform.product.service.ProductService;
import com.wealth.platform.product.vo.ProductVO;
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

    private final ProductService productService;

    @Operation(summary = "根据ID查询产品")
    @GetMapping("/{id}")
    public Result<ProductVO> getById(@PathVariable Long id) {
        ProductVO vo = productService.getProductById(id);
        if (vo == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(vo);
    }

    @Operation(summary = "查询产品列表")
    @GetMapping
    public Result<List<ProductVO>> list() {
        return Result.success(productService.getProductList());
    }

    @Operation(summary = "分页查询产品")
    @GetMapping("/page")
    public Result<IPage<ProductVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) Integer productType,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderDir) {
        Page<WeaProduct> page = new Page<>(pageNum, pageSize);
        return Result.success(productService.pageProducts(page, productName, productCode, productType, orderBy, orderDir));
    }

    @Operation(summary = "创建产品")
    @PostMapping
    @AuditLog(module = "产品管理", operation = "创建产品")
    @AntiReplay
    public Result<Boolean> create(@Valid @RequestBody ProductDTO dto) {
        return Result.success(productService.createProduct(dto));
    }

    @Operation(summary = "更新产品信息")
    @PutMapping("/{id}")
    @AuditLog(module = "产品管理", operation = "更新产品")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        return Result.success(productService.updateProduct(id, dto));
    }

    @Operation(summary = "删除产品")
    @DeleteMapping("/{id}")
    @AuditLog(module = "产品管理", operation = "删除产品")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(productService.deleteProduct(id));
    }

}