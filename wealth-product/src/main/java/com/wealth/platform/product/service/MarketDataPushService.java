package com.wealth.platform.product.service;

import com.wealth.platform.product.vo.FinMarketDataVO;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SSE 行情推送服务，管理客户端连接并广播实时行情。
 */
@Slf4j
@Service
public class MarketDataPushService {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * 创建 SSE 连接，超时 86400 秒（24 小时）。
     */
    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(86400_000L);
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
     */
    public void broadcastMarketUpdate(List<FinMarketDataVO> marketDataList) {
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
                    emitter.complete();
                } catch (Exception ex) {
                    log.warn("SSE emitter.complete() 异常", ex);
                }
            }
        }
    }

    /** 当前客户端连接数 */
    public int getEmitterCount() {
        return emitters.size();
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
