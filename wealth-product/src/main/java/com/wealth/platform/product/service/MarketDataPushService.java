package com.wealth.platform.product.service;

import com.wealth.platform.product.vo.FinMarketDataVO;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Iterator;
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

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

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
     * 发送失败时立即移除对应 emitter，避免僵尸连接堆积。
     */
    public void broadcastMarketUpdate(List<FinMarketDataVO> marketDataList) {
        if (emitters.isEmpty()) return;

        Iterator<SseEmitter> it = emitters.iterator();
        while (it.hasNext()) {
            SseEmitter emitter = it.next();
            try {
                emitter.send(SseEmitter.event()
                        .name("market-update")
                        .data(marketDataList));
            } catch (IOException e) {
                it.remove();
                log.warn("SSE 推送失败，已移除连接: {}", e.getMessage());
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.warn("SSE emitter.completeWithError() 异常", ex);
                }
            }
        }
    }

    /** 当前客户端连接数 */
    public int getEmitterCount() {
        removeDeadEmitters();
        return emitters.size();
    }

    /**
     * 遍历并移除已断开的 emitter（通过 send ping 检测）。
     * 仅在 createEmitter 和 getEmitterCount 时调用，不干扰高频广播路径。
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
