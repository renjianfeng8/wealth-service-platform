package com.wealth.platform.message.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.wealth.platform.message.config.RabbitMqConfig.QUEUE_MSG_PUSH;

/**
 * RabbitMQ 消息消费者
 * 消费消息推送队列，处理失败时 Spring Retry 会自动重试
 * 重试耗尽后消息自动进入死信队列(DLQ)
 */
@Slf4j
@Component
public class FinMessageConsumer {

    @RabbitListener(queues = QUEUE_MSG_PUSH)
    public void handleMsgPush(String messageBody, Message message, Channel channel) {
        log.info("消费消息推送 | body={}", messageBody);

        // 异步消息处理抛异常时会触发重试，重试耗尽后自动进入 DLQ
        // 当前实现为日志记录，后续可扩展为 WebSocket 推送、手机推送等
        log.debug("消息处理完成 | messageId={}", message.getMessageProperties().getMessageId());
    }
}
