package com.wealth.platform.product.service;

import com.wealth.platform.product.vo.MarketDataVO;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SSE 行情推送服务，管理客户端连接并广播实时行情。
 * 连接上限 MAX_EMITTERS=500，超限时拒绝新连接防止 OOM。
 */
@Slf4j
@Service
public class MarketDataPushService {

    private static final int MAX_EMITTERS = 500;

    @Value("${market.sse.timeout:86400000}")
    private Long sseTimeout;

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private final MarketDataSimulationService marketDataSimulationService;

    /**
     * 与 {@link MarketDataSimulationService} 互为依赖（订阅首推快照），
     * 故通过 @Lazy 注入该依赖以打破构造器循环依赖。
     */
    public MarketDataPushService(@Lazy MarketDataSimulationService marketDataSimulationService) {
        this.marketDataSimulationService = marketDataSimulationService;
    }

    /**
     * 建立 SSE 订阅：创建连接并推送一次全量快照（快照取自行情模拟服务缓存）。
     * 快照获取或首推失败均不中断连接建立，由 Controller 一行委托。
     */
    public SseEmitter subscribe() {
        SseEmitter emitter = createEmitter();
        try {
            List<MarketDataVO> snapshot = marketDataSimulationService.getAllMarketData();
            emitter.send(SseEmitter.event()
                    .name("market-update")
                    .data(snapshot));
        } catch (Exception e) {
            log.warn("SSE 首次推送快照异常", e);
        }
        return emitter;
    }

    /**
     * 创建 SSE 连接，超时 86400 秒（24 小时）。
     * 达到上限时移除已断开的连接；若仍超限则拒绝新连接。
     */
    public SseEmitter createEmitter() {
        removeDeadEmitters();

        if (emitters.size() >= MAX_EMITTERS) {
            log.warn("SSE 连接数已达上限 {}, 拒绝新连接", MAX_EMITTERS);
            throw new com.wealth.common.exception.ServiceException(503, "SSE 连接数已达上限，请稍后重试");
        }

        SseEmitter emitter = new SseEmitter(sseTimeout);
        emitters.add(emitter);

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.info("SSE 连接完成，当前连接数: {}", emitters.size());
        });
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.info("SSE 连接超时，当前连接数: {}", emitters.size());
        });
        emitter.onError(e -> {
            emitters.remove(emitter);
            log.warn("SSE 连接异常: {}", e.getMessage());
        });

        log.info("新建 SSE 连接，当前连接数: {}", emitters.size());
        return emitter;
    }

    /**
     * 向所有客户端广播完整行情快照。
     * 发送失败时立即移除对应 emitter，避免僵尸连接堆积。
     */
    public void broadcastMarketUpdate(List<MarketDataVO> marketDataList) {
        if (emitters.isEmpty()) return;

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("market-update")
                        .data(marketDataList));
            } catch (IOException e) {
                emitters.remove(emitter);
                log.warn("SSE 推送失败，已移除连接: {}", e.getMessage());
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.warn("SSE emitter.completeWithError() 异常", ex);
                }
            }
        }
    }

    /**
     * 遍历并移除已断开的 emitter（通过 send ping 检测）。
     * 仅在 createEmitter 时调用，不干扰高频广播路径。
     */
    private void removeDeadEmitters() {
        emitters.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("ping").data(""));
                return false;
            } catch (IOException e) {
                return true;
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.warn("SSE shutdown 关闭 emitter 异常", e);
            }
        }
        emitters.clear();
        log.info("SSE 推送服务关闭，已清理所有连接");
    }
}
