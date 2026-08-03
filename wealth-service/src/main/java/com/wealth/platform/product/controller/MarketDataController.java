package com.wealth.platform.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.product.dto.MarketDataDTO;
import com.wealth.platform.product.entity.WeaMarketData;
import com.wealth.platform.product.service.MarketDataService;
import com.wealth.platform.product.service.MarketDataPushService;
import com.wealth.platform.product.vo.MarketDataVO;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@Tag(name = "行情管理", description = "行情数据相关接口")
@RequestMapping("/product/wea-market-data")
@RequiredArgsConstructor
@Validated
public class MarketDataController {

    private final MarketDataService marketDataService;
    private final MarketDataPushService marketDataPushService;

    @Operation(summary = "SSE 实时行情推送（JWT 由 Gateway 校验或 httpOnly Cookie 携带）")
    @GetMapping("/sse")
    public SseEmitter subscribe() {
        // JWT 身份认证由 Gateway 统一处理，此处不再单独校验
        return marketDataPushService.subscribe();
    }

    @Operation(summary = "分页查询行情数据")
    @GetMapping("/page")
    public Result<IPage<MarketDataVO>> page(
            @Min(1) @RequestParam(defaultValue = "1") Integer pageNum,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String productCode) {
        IPage<WeaMarketData> page = marketDataService.pageWithFilter(pageNum, pageSize, productCode);
        return Result.success(BeanConvertUtil.convertPage(page, MarketDataVO.class));
    }

    @Operation(summary = "创建行情数据")
    @PostMapping
    @AuditLog(module = "行情管理", operation = "创建行情数据")
    @AntiReplay
    public Result<Boolean> create(@Valid @RequestBody MarketDataDTO dto) {
        return Result.success(marketDataService.createMarketData(dto));
    }

    @Operation(summary = "更新行情数据")
    @PutMapping("/{id}")
    @AuditLog(module = "行情管理", operation = "更新行情数据")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody MarketDataDTO dto) {
        return Result.success(marketDataService.updateMarketData(id, dto));
    }

    @Operation(summary = "删除行情数据")
    @DeleteMapping("/{id}")
    @AuditLog(module = "行情管理", operation = "删除行情数据")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(marketDataService.deleteMarketData(id));
    }
}