package com.wealth.platform.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.platform.product.dto.MarketDataDTO;
import com.wealth.platform.product.entity.WeaMarketData;
import com.wealth.platform.product.service.MarketDataService;
import com.wealth.platform.product.service.MarketDataPushService;
import com.wealth.platform.product.service.MarketDataSimulationService;
import com.wealth.platform.product.vo.MarketDataVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@RestController
@Tag(name = "行情管理", description = "行情数据相关接口")
@RequestMapping("/product/wea-market-data")
@RequiredArgsConstructor
@Validated
public class MarketDataController {

    private final MarketDataService marketDataService;
    private final MarketDataPushService marketDataPushService;
    private final MarketDataSimulationService marketDataSimulationService;

    @Operation(summary = "根据ID查询行情数据")
    @GetMapping("/{id}")
    public Result<MarketDataVO> getById(@PathVariable Long id) {
        MarketDataVO vo = marketDataService.getMarketDataById(id);
        if (vo == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(vo);
    }

    @Operation(summary = "查询行情数据列表")
    @GetMapping
    public Result<List<MarketDataVO>> list() {
        return Result.success(marketDataService.getMarketDataList());
    }

    @Operation(summary = "SSE 实时行情推送（JWT 由 Gateway 校验或 httpOnly Cookie 携带）")
    @GetMapping("/sse")
    public SseEmitter subscribe() {
        // JWT 身份认证由 Gateway 统一处理，此处不再单独校验
        // 先推送全量快照
        List<MarketDataVO> snapshot = marketDataSimulationService.getAllMarketData();
        SseEmitter emitter = marketDataPushService.createEmitter();
        try {
            emitter.send(SseEmitter.event()
                    .name("market-update")
                    .data(snapshot));
        } catch (Exception e) {
            log.warn("SSE 首次推送快照异常, emitterId: {}", emitter, e);
        }
        return emitter;
    }

    @Operation(summary = "分页查询行情数据")
    @GetMapping("/page")
    public Result<IPage<MarketDataVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
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