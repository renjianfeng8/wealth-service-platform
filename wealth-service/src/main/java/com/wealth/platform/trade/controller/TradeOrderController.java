package com.wealth.platform.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
import com.wealth.platform.trade.dto.TradeOrderDTO;
import com.wealth.platform.trade.dto.TradeOrderStatusDTO;
import com.wealth.platform.trade.entity.WeaTradeOrder;
import com.wealth.platform.trade.service.TradeOrderService;
import com.wealth.platform.trade.vo.TradeOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trade/wea-trade-order")
@Tag(name = "交易委托管理", description = "wea_trade_order 交易委托相关接口")
@RequiredArgsConstructor
@Validated
public class TradeOrderController {

    private final TradeOrderService tradeOrderService;

    @Operation(summary = "根据ID查询交易委托单")
    @GetMapping("/{id}")
    public Result<TradeOrderVO> getById(@PathVariable Long id) {
        return Result.success(tradeOrderService.getOrderById(id));
    }

    @Operation(summary = "分页查询交易委托单")
    @GetMapping("/page")
    public Result<IPage<TradeOrderVO>> page(
            @Min(1) @RequestParam(defaultValue = "1") Integer pageNum,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) Integer orderStatus) {
        Page<WeaTradeOrder> page = new Page<>(pageNum, pageSize);
        return Result.success(tradeOrderService.pageOrders(page, userId, orderNo, productCode, orderStatus));
    }

    @Operation(summary = "创建交易委托单（支持幂等键防重）")
    @PostMapping
    @AuditLog(module = "交易管理", operation = "创建委托单")
    @AntiReplay
    public Result<Boolean> create(@Valid @RequestBody TradeOrderDTO dto) {
        return Result.success(tradeOrderService.createOrder(dto));
    }

    @Operation(summary = "更新交易委托单信息")
    @PutMapping("/{id}")
    @AuditLog(module = "交易管理", operation = "更新委托单")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody TradeOrderDTO dto) {
        return Result.success(tradeOrderService.updateOrder(id, dto));
    }

    @Operation(summary = "更新交易委托单状态（含状态机校验）")
    @PutMapping("/{id}/status")
    @AuditLog(module = "交易管理", operation = "更新委托单状态")
    public Result<Boolean> updateStatus(@PathVariable Long id, @Valid @RequestBody TradeOrderStatusDTO dto) {
        return Result.success(tradeOrderService.updateOrderStatus(id, dto));
    }

    @Operation(summary = "删除交易委托单（逻辑删除）")
    @DeleteMapping("/{id}")
    @AuditLog(module = "交易管理", operation = "删除委托单")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(tradeOrderService.deleteOrder(id));
    }
}
