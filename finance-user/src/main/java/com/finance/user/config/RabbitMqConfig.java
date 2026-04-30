package com.finance.user.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    // ------------------- 交易通知 -------------------
    @Bean
    /**
     * 创建交易通知队列
     *
     * @return RabbitMQ 队列
     */
    public Queue tradeNotifyQueue() {
        return new Queue(QUEUE_TRADE_NOTIFY, true);
    }

    @Bean
    /**
     * 创建交易通知交换机（DirectExchange）
     *
     * @return 交换机
     */
    public DirectExchange tradeExchange() {
        return new DirectExchange(EXCHANGE_TRADE);
    }

    @Bean
    /**
     * 绑定交易通知队列与交换机
     *
     * @param tradeNotifyQueue 交易通知队列
     * @param tradeExchange 交易交换机
     * @return 绑定关系
     */
    public Binding tradeNotifyBinding(Queue tradeNotifyQueue, DirectExchange tradeExchange) {
        return BindingBuilder.bind(tradeNotifyQueue)
                .to(tradeExchange)
                .with(ROUTING_KEY_TRADE);
    }

    // ------------------- 消息推送 -------------------
    @Bean
    /**
     * 创建消息推送队列
     *
     * @return RabbitMQ 队列
     */
    public Queue msgPushQueue() {
        return new Queue(QUEUE_MSG_PUSH, true);
    }

    @Bean
    /**
     * 创建消息推送交换机（DirectExchange）
     *
     * @return 交换机
     */
    public DirectExchange msgExchange() {
        return new DirectExchange(EXCHANGE_MSG);
    }

    @Bean
    /**
     * 绑定消息推送队列与交换机
     *
     * @param msgPushQueue 消息推送队列
     * @param msgExchange 消息交换机
     * @return 绑定关系
     */
    public Binding msgPushBinding(Queue msgPushQueue, DirectExchange msgExchange) {
        return BindingBuilder.bind(msgPushQueue)
                .to(msgExchange)
                .with(ROUTING_KEY_MSG);
    }
}