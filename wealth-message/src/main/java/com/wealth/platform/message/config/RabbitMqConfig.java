package com.wealth.platform.message.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 队列与交换机配置
 * 包含死信队列(DLX)配置，确保消息消费失败后的可靠性
 */
@Configuration
public class RabbitMqConfig {

    // ==================== 交易通知队列 ====================
    public static final String QUEUE_TRADE_NOTIFY = "trade_notify_queue";
    public static final String QUEUE_TRADE_NOTIFY_DLQ = "trade_notify_dlq";
    public static final String EXCHANGE_TRADE = "trade_exchange";
    public static final String EXCHANGE_TRADE_DLX = "trade_dlx";
    public static final String ROUTING_KEY_TRADE = "trade.notify";
    public static final String ROUTING_KEY_TRADE_DLQ = "trade.notify.dlq";

    // ==================== 消息推送队列 ====================
    public static final String QUEUE_MSG_PUSH = "msg_push_queue";
    public static final String QUEUE_MSG_PUSH_DLQ = "msg_push_dlq";
    public static final String EXCHANGE_MSG = "msg_exchange";
    public static final String EXCHANGE_MSG_DLX = "msg_dlx";
    public static final String ROUTING_KEY_MSG = "msg.push";
    public static final String ROUTING_KEY_MSG_DLQ = "msg.push.dlq";

    // ==================== 交易通知队列：主队列 + DLX ====================
    @Bean
    public Queue tradeNotifyQueue() {
        return QueueBuilder.durable(QUEUE_TRADE_NOTIFY)
                .deadLetterExchange(EXCHANGE_TRADE_DLX)
                .deadLetterRoutingKey(ROUTING_KEY_TRADE_DLQ)
                .build();
    }

    @Bean
    public DirectExchange tradeExchange() {
        return new DirectExchange(EXCHANGE_TRADE);
    }

    @Bean
    public Binding tradeNotifyBinding(Queue tradeNotifyQueue, DirectExchange tradeExchange) {
        return BindingBuilder.bind(tradeNotifyQueue)
                .to(tradeExchange)
                .with(ROUTING_KEY_TRADE);
    }

    @Bean
    public Queue tradeNotifyDlq() {
        return QueueBuilder.durable(QUEUE_TRADE_NOTIFY_DLQ).build();
    }

    @Bean
    public DirectExchange tradeDlx() {
        return new DirectExchange(EXCHANGE_TRADE_DLX);
    }

    @Bean
    public Binding tradeNotifyDlqBinding(Queue tradeNotifyDlq, DirectExchange tradeDlx) {
        return BindingBuilder.bind(tradeNotifyDlq)
                .to(tradeDlx)
                .with(ROUTING_KEY_TRADE_DLQ);
    }

    // ==================== 消息推送队列：主队列 + DLX ====================
    @Bean
    public Queue msgPushQueue() {
        return QueueBuilder.durable(QUEUE_MSG_PUSH)
                .deadLetterExchange(EXCHANGE_MSG_DLX)
                .deadLetterRoutingKey(ROUTING_KEY_MSG_DLQ)
                .build();
    }

    @Bean
    public DirectExchange msgExchange() {
        return new DirectExchange(EXCHANGE_MSG);
    }

    @Bean
    public Binding msgPushBinding(Queue msgPushQueue, DirectExchange msgExchange) {
        return BindingBuilder.bind(msgPushQueue)
                .to(msgExchange)
                .with(ROUTING_KEY_MSG);
    }

    @Bean
    public Queue msgPushDlq() {
        return QueueBuilder.durable(QUEUE_MSG_PUSH_DLQ).build();
    }

    @Bean
    public DirectExchange msgDlx() {
        return new DirectExchange(EXCHANGE_MSG_DLX);
    }

    @Bean
    public Binding msgPushDlqBinding(Queue msgPushDlq, DirectExchange msgDlx) {
        return BindingBuilder.bind(msgPushDlq)
                .to(msgDlx)
                .with(ROUTING_KEY_MSG_DLQ);
    }
}
