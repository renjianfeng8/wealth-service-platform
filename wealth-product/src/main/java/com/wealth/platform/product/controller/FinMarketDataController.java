package com.wealth.platform.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.audit.AntiReplay;
import com.wealth.common.audit.AuditLog;
import com.wealth.common.result.Result;
import com.wealth.common.result.ResultCode;
import com.wealth.common.utils.BeanConvertUtil;
import com.wealth.common.utils.JwtUtil;
import com.wealth.platform.product.dto.FinMarketDataDTO;
import com.wealth.platform.product.entity.WeaMarketData;
import com.wealth.platform.product.service.FinMarketDataService;
import com.wealth.platform.product.service.MarketDataPushService;
import com.wealth.platform.product.service.MarketDataSimulationService;
import com.wealth.platform.product.vo.FinMarketDataVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@Tag(name = "琛屾儏绠＄悊", description = "琛屾儏鏁版嵁鐩稿叧鎺ュ彛")
@RequestMapping("/WeaMarketData")
@RequiredArgsConstructor
public class FinMarketDataController {

    private final FinMarketDataService finMarketDataService;
    private final JwtUtil jwtUtil;
    private final MarketDataPushService marketDataPushService;
    private final MarketDataSimulationService marketDataSimulationService;

    @Operation(summary = "鏍规嵁ID鏌ヨ琛屾儏鏁版嵁")
    @GetMapping("/{id}")
    public Result<FinMarketDataVO> getById(@PathVariable Long id) {
        FinMarketDataVO vo = finMarketDataService.getMarketDataById(id);
        if (vo == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(vo);
    }

    @Operation(summary = "鏌ヨ琛屾儏鏁版嵁鍒楄〃")
    @GetMapping
    public Result<List<FinMarketDataVO>> list() {
        return Result.success(finMarketDataService.getMarketDataList());
    }

    @Operation(summary = "SSE 实时行情推送")
    @GetMapping("/sse")
    @CrossOrigin
    public SseEmitter subscribe(@RequestParam String token) {
        if (!jwtUtil.validateToken(token)) {
            // 无法直接返回 Result，通过异常处理返回 401
            throw new com.wealth.common.exception.ServiceException(401, "无效的 Token");
        }
        // 先推送全量快照
        List<FinMarketDataVO> snapshot = marketDataSimulationService.getAllMarketData();
        SseEmitter emitter = marketDataPushService.createEmitter();
        try {
            emitter.send(SseEmitter.event()
                    .name("market-update")
                    .data(snapshot));
        } catch (Exception e) {
            // 忽略首次推送异常
        }
        return emitter;
    }

    @Operation(summary = "鍒嗛〉鏌ヨ琛屾儏鏁版嵁")
    @GetMapping("/page")
    public Result<IPage<FinMarketDataVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<WeaMarketData> page = new Page<>(pageNum, pageSize);
        IPage<WeaMarketData> entityPage = finMarketDataService.page(page);
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
    @AuditLog(module = "行情管理", operation = "创建行情数据")
    @AntiReplay
    public Result<Boolean> create(@Valid @RequestBody FinMarketDataDTO dto) {
        return Result.success(finMarketDataService.createMarketData(dto));
    }

    @Operation(summary = "更新行情数据")
    @PutMapping("/{id}")
    @AuditLog(module = "行情管理", operation = "更新行情数据")
    @AntiReplay
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody FinMarketDataDTO dto) {
        return Result.success(finMarketDataService.updateMarketData(id, dto));
    }

    @Operation(summary = "删除行情数据")
    @DeleteMapping("/{id}")
    @AuditLog(module = "行情管理", operation = "删除行情数据")
    @AntiReplay
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(finMarketDataService.deleteMarketData(id));
    }
}