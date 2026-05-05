package com.finance.platform.message.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 队列与交换机配置
 * 原位于 finance-user 模块，迁移至 finance-message 模块以更合理分配职责
 */
@Configuration
public class RabbitMqConfig {

    // 交易通知队列
    public static final String QUEUE_TRADE_NOTIFY = "trade_notify_queue";
    public static final String EXCHANGE_TRADE = "trade_exchange";
    public static final String ROUTING_KEY_TRADE = "trade.notify";

    // 消息推送队列
    public static final String QUEUE_MSG_PUSH = "msg_push_queue";
    public static final String EXCHANGE_MSG = "msg_exchange";
    public static final String ROUTING_KEY_MSG = "msg.push";

    @Bean
    public Queue tradeNotifyQueue() {
        return new Queue(QUEUE_TRADE_NOTIFY, true);
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
    public Queue msgPushQueue() {
        return new Queue(QUEUE_MSG_PUSH, true);
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
}
