package com.finance.platform.trade.controller;

import com.finance.common.result.Result;
import com.finance.platform.trade.dto.FinTradeOrderDTO;
import com.finance.platform.trade.service.FinTradeOrderService;
import com.finance.platform.trade.vo.FinTradeOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class FinTradeOrderController {

    private final FinTradeOrderService finTradeOrderService;

    /**
     * 根据 ID 查询交易委托单信息。
     *
     * @param id 交易委托单 ID
     * @return 查询结果
     */
    @Operation(summary = "根据ID查询交易委托单")
    @GetMapping("/{id}")
    public Result<FinTradeOrderVO> getById(@PathVariable Long id) {
        return Result.success(finTradeOrderService.getOrderById(id));
    }

    /**
     * 查询交易委托单列表（不分页）。
     *
     * @return 交易委托单列表
     */
    @Operation(summary = "查询交易委托单列表")
    @GetMapping
    public Result<List<FinTradeOrderVO>> list() {
        return Result.success(finTradeOrderService.getOrderList());
    }

    /**
     * 创建交易委托单。
     *
     * @param dto 交易委托单入参
     * @return 是否创建成功
     */
    @Operation(summary = "创建交易委托单")
    @PostMapping
    public Result<Boolean> create(@RequestBody FinTradeOrderDTO dto) {
        return Result.success(finTradeOrderService.createOrder(dto));
    }

    /**
     * 更新交易委托单信息。
     *
     * @param id 交易委托单 ID
     * @param dto 交易委托单入参
     * @return 是否更新成功
     */
    @Operation(summary = "更新交易委托单信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody FinTradeOrderDTO dto) {
        return Result.success(finTradeOrderService.updateOrder(id, dto));
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
        return Result.success(finTradeOrderService.deleteOrder(id));
    }
}