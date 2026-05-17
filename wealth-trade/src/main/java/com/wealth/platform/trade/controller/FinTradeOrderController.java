package com.wealth.platform.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.platform.trade.dto.FinTradeOrderDTO;
import com.wealth.platform.trade.dto.FinTradeOrderStatusDTO;
import com.wealth.platform.trade.entity.WeaTradeOrder;
import com.wealth.platform.trade.service.FinTradeOrderService;
import com.wealth.platform.trade.vo.FinTradeOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/WeaTradeOrder")
@Tag(name = "交易委托管理", description = "wea_trade_order 交易委托相关接口")
@RequiredArgsConstructor
public class FinTradeOrderController {

    private final FinTradeOrderService finTradeOrderService;

    @Operation(summary = "根据ID查询交易委托单")
    @GetMapping("/{id}")
    public Result<FinTradeOrderVO> getById(@PathVariable Long id) {
        FinTradeOrderVO vo = finTradeOrderService.getOrderById(id);
        if (vo == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(vo);
    }

    @Operation(summary = "查询交易委托单列表")
    @GetMapping
    public Result<List<FinTradeOrderVO>> list() {
        return Result.success(finTradeOrderService.getOrderList());
    }

    @Operation(summary = "分页查询交易委托单")
    @GetMapping("/page")
    public Result<IPage<FinTradeOrderVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer orderStatus) {
        Page<WeaTradeOrder> page = new Page<>(pageNum, pageSize);
        return Result.success(finTradeOrderService.pageOrders(page, userId, orderStatus));
    }

    @Operation(summary = "创建交易委托单（支持幂等键防重）")
    @PostMapping
    public Result<Boolean> create(@Valid @RequestBody FinTradeOrderDTO dto) {
        return Result.success(finTradeOrderService.createOrder(dto));
    }

    @Operation(summary = "更新交易委托单信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody FinTradeOrderDTO dto) {
        return Result.success(finTradeOrderService.updateOrder(id, dto));
    }

    @Operation(summary = "更新交易委托单状态（含状态机校验）")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Long id, @Valid @RequestBody FinTradeOrderStatusDTO dto) {
        return Result.success(finTradeOrderService.updateOrderStatus(id, dto));
    }

    @Operation(summary = "删除交易委托单（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(finTradeOrderService.deleteOrder(id));
    }
}
