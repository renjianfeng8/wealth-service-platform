package com.wealth.platform.product.service;

import com.wealth.platform.product.vo.MarketDataVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MarketDataPushServiceTest {

    private MarketDataPushService pushService;

    @BeforeEach
    void setUp() {
        pushService = new MarketDataPushService();
        ReflectionTestUtils.setField(pushService, "sseTimeout", 1000L);
    }

    @Test
    void broadcastMarketUpdate_should_remove_emitter_when_send_fails() throws Exception {
        SseEmitter emitter = mock(SseEmitter.class);
        doThrow(new IOException("client closed")).when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
        emitters.add(emitter);
        ReflectionTestUtils.setField(pushService, "emitters", emitters);

        pushService.broadcastMarketUpdate(List.of(new MarketDataVO()));

        assertEquals(0, emitters.size());
        verify(emitter).completeWithError(any(IOException.class));
    }
}
