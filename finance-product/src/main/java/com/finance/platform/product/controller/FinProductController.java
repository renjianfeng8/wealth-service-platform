package com.finance.platform.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.common.result.Result;
import com.finance.common.result.ResultCode;
import com.finance.common.utils.BeanConvertUtil;
import com.finance.platform.product.dto.FinProductDTO;
import com.finance.platform.product.entity.FinProduct;
import com.finance.platform.product.service.FinProductService;
import com.finance.platform.product.vo.FinProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "产品管理", description = "产品相关接口")
@RequestMapping("/finProduct")
@RequiredArgsConstructor
public class FinProductController {

    private final FinProductService finProductService;

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
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<FinProduct> page = new Page<>(pageNum, pageSize);
        IPage<FinProduct> entityPage = finProductService.page(page);
        Page<FinProductVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());
        voPage.setPages(entityPage.getPages());
        voPage.setRecords(BeanConvertUtil.convertList(entityPage.getRecords(), FinProductVO.class));
        return Result.success(voPage);
    }

    @Operation(summary = "创建产品")
    @PostMapping
    public Result<Boolean> create(@Valid @RequestBody FinProductDTO dto) {
        return Result.success(finProductService.createProduct(dto));
    }

    @Operation(summary = "更新产品信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody FinProductDTO dto) {
        return Result.success(finProductService.updateProduct(id, dto));
    }

    @Operation(summary = "删除产品")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(finProductService.deleteProduct(id));
    }
}