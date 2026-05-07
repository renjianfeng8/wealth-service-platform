package com.finance.platform.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.common.result.Result;
import com.finance.common.result.ResultCode;
import com.finance.common.utils.BeanConvertUtil;
import com.finance.platform.product.dto.FinMarketDataDTO;
import com.finance.platform.product.entity.FinMarketData;
import com.finance.platform.product.service.FinMarketDataService;
import com.finance.platform.product.vo.FinMarketDataVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "行情管理", description = "行情数据相关接口")
@RequestMapping("/finMarketData")
@RequiredArgsConstructor
public class FinMarketDataController {

    private final FinMarketDataService finMarketDataService;

    @Operation(summary = "根据ID查询行情数据")
    @GetMapping("/{id}")
    public Result<FinMarketDataVO> getById(@PathVariable Long id) {
        FinMarketDataVO vo = finMarketDataService.getMarketDataById(id);
        if (vo == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(vo);
    }

    @Operation(summary = "查询行情数据列表")
    @GetMapping
    public Result<List<FinMarketDataVO>> list() {
        return Result.success(finMarketDataService.getMarketDataList());
    }

    @Operation(summary = "分页查询行情数据")
    @GetMapping("/page")
    public Result<IPage<FinMarketDataVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<FinMarketData> page = new Page<>(pageNum, pageSize);
        IPage<FinMarketData> entityPage = finMarketDataService.page(page);
        Page<FinMarketDataVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());
        voPage.setPages(entityPage.getPages());
        voPage.setRecords(BeanConvertUtil.convertList(entityPage.getRecords(), FinMarketDataVO.class));
        return Result.success(voPage);
    }

    @Operation(summary = "创建行情数据")
    @PostMapping
    public Result<Boolean> create(@Valid @RequestBody FinMarketDataDTO dto) {
        return Result.success(finMarketDataService.createMarketData(dto));
    }

    @Operation(summary = "更新行情数据")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody FinMarketDataDTO dto) {
        return Result.success(finMarketDataService.updateMarketData(id, dto));
    }

    @Operation(summary = "删除行情数据")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(finMarketDataService.deleteMarketData(id));
    }
}