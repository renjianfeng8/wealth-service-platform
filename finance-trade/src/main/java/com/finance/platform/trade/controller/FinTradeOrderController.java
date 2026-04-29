package com.finance.platform.trade.controller;

import com.finance.common.result.Result;
import com.finance.platform.trade.entity.FinTradeOrder;
import com.finance.platform.trade.service.FinTradeOrderService;
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
 * 交易委托单控制器。
 */
@RestController
@Tag(name = "交易委托管理", description = "fin_trade_order 交易委托相关接口")
@RequestMapping("/finTradeOrder")
public class FinTradeOrderController {

    private final FinTradeOrderService finTradeOrderService;

    /**
     * 交易委托单控制器构造器。
     *
     * @param finTradeOrderService 交易委托单业务服务
     */
    public FinTradeOrderController(FinTradeOrderService finTradeOrderService) {
        this.finTradeOrderService = finTradeOrderService;
    }

    /**
     * 根据 ID 查询交易委托单信息。
     *
     * @param id 交易委托单 ID
     * @return 查询结果
     */
    @Operation(summary = "根据ID查询交易委托单")
    @GetMapping("/{id}")
    public Result<FinTradeOrder> getById(@PathVariable Long id) {
        return Result.success(finTradeOrderService.getById(id));
    }

    /**
     * 查询交易委托单列表（不分页）。
     *
     * @return 交易委托单列表
     */
    @Operation(summary = "查询交易委托单列表")
    @GetMapping
    public Result<List<FinTradeOrder>> list() {
        return Result.success(finTradeOrderService.list());
    }

    /**
     * 创建交易委托单。
     *
     * @param finTradeOrder 交易委托单入参
     * @return 是否创建成功
     */
    @Operation(summary = "创建交易委托单")
    @PostMapping
    public Result<Boolean> create(@RequestBody FinTradeOrder finTradeOrder) {
        boolean saved = finTradeOrderService.save(finTradeOrder);
        return Result.success(saved);
    }

    /**
     * 更新交易委托单信息。
     *
     * @param id 交易委托单 ID
     * @param finTradeOrder 交易委托单入参
     * @return 是否更新成功
     */
    @Operation(summary = "更新交易委托单信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody FinTradeOrder finTradeOrder) {
        finTradeOrder.setId(id);
        boolean updated = finTradeOrderService.updateById(finTradeOrder);
        return Result.success(updated);
    }

    /**
     * 删除交易委托单（逻辑删除）。
     *
     * @param id 交易委托单 ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除交易委托单（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = finTradeOrderService.removeById(id);
        return Result.success(removed);
    }
}

