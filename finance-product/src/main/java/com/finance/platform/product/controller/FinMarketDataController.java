package com.finance.platform.product.controller;

import com.finance.common.result.Result;
import com.finance.platform.product.entity.FinMarketData;
import com.finance.platform.product.service.FinMarketDataService;
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
 * 行情数据表控制器。
 */
@RestController
@Tag(name = "行情管理", description = "fin_market_data 行情相关接口")
@RequestMapping("/finMarketData")
public class FinMarketDataController {

    private final FinMarketDataService finMarketDataService;

    /**
     * 行情数据表控制器构造器。
     *
     * @param finMarketDataService 行情业务服务
     */
    public FinMarketDataController(FinMarketDataService finMarketDataService) {
        this.finMarketDataService = finMarketDataService;
    }

    /**
     * 根据 ID 查询行情数据。
     *
     * @param id 行情 ID
     * @return 查询结果
     */
    @Operation(summary = "根据ID查询行情数据")
    @GetMapping("/{id}")
    public Result<FinMarketData> getById(@PathVariable Long id) {
        return Result.success(finMarketDataService.getById(id));
    }

    /**
     * 查询行情数据列表（不分页）。
     *
     * @return 行情列表
     */
    @Operation(summary = "查询行情数据列表")
    @GetMapping
    public Result<List<FinMarketData>> list() {
        return Result.success(finMarketDataService.list());
    }

    /**
     * 创建行情数据。
     *
     * @param finMarketData 行情入参
     * @return 是否创建成功
     */
    @Operation(summary = "创建行情数据")
    @PostMapping
    public Result<Boolean> create(@RequestBody FinMarketData finMarketData) {
        boolean saved = finMarketDataService.save(finMarketData);
        return Result.success(saved);
    }

    /**
     * 更新行情数据。
     *
     * @param id 行情 ID
     * @param finMarketData 行情入参
     * @return 是否更新成功
     */
    @Operation(summary = "更新行情数据")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody FinMarketData finMarketData) {
        finMarketData.setId(id);
        boolean updated = finMarketDataService.updateById(finMarketData);
        return Result.success(updated);
    }

    /**
     * 删除行情数据（逻辑删除）。
     *
     * @param id 行情 ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除行情数据（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = finMarketDataService.removeById(id);
        return Result.success(removed);
    }
}

