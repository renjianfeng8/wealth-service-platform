package com.wealth.platform.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.product.dto.FinProductDTO;
import com.wealth.platform.product.entity.WeaProduct;
import com.wealth.platform.product.service.FinProductService;
import com.wealth.platform.product.vo.FinProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "浜у搧绠＄悊", description = "浜у搧鐩稿叧鎺ュ彛")
@RequestMapping("/WeaProduct")
@RequiredArgsConstructor
public class FinProductController {

    private final FinProductService finProductService;

    @Operation(summary = "鏍规嵁ID鏌ヨ浜у搧")
    @GetMapping("/{id}")
    public Result<FinProductVO> getById(@PathVariable Long id) {
        FinProductVO vo = finProductService.getProductById(id);
        if (vo == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(vo);
    }

    @Operation(summary = "鏌ヨ浜у搧鍒楄〃")
    @GetMapping
    public Result<List<FinProductVO>> list() {
        return Result.success(finProductService.getProductList());
    }

    @Operation(summary = "鍒嗛〉鏌ヨ浜у搧")
    @GetMapping("/page")
    public Result<IPage<FinProductVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer productType) {
        Page<WeaProduct> page = new Page<>(pageNum, pageSize);
        return Result.success(finProductService.pageProducts(page, productType));
    }

    @Operation(summary = "鍒涘缓浜у搧")
    @PostMapping
    public Result<Boolean> create(@Valid @RequestBody FinProductDTO dto) {
        return Result.success(finProductService.createProduct(dto));
    }

    @Operation(summary = "鏇存柊浜у搧淇℃伅")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody FinProductDTO dto) {
        return Result.success(finProductService.updateProduct(id, dto));
    }

    @Operation(summary = "鍒犻櫎浜у搧")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(finProductService.deleteProduct(id));
    }
}