package com.wealth.platform.message.mq;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 消息生产者，支持 Publisher Confirm 和 Return Callback
 * 确保消息可靠投递：ConfirmCallback 确认消息是否到达交换机
 * ReturnsCallback 处理路由到队列失败的消息
 */
@Slf4j
@Component
public class RabbitMqProducer implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
        rabbitTemplate.setMandatory(true);
    }

    /**
     * 发送消息到指定交换机
     *
     * @param exchange   交换机
     * @param routingKey 路由键
     * @param message    消息内容（JSON 字符串）
     */
    public void send(String exchange, String routingKey, String message) {
        CorrelationData correlationData = new CorrelationData();
        rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
        log.info("消息已发送 | exchange={} | routingKey={} | data={} | correlationId={}",
                exchange, routingKey, message, correlationData.getId());
    }

    /**
     * Publisher Confirm：确认消息是否成功到达交换机
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            log.debug("消息确认成功 | correlationId={}", correlationData.getId());
        } else {
            log.error("消息确认失败 | correlationId={} | cause={}", correlationData.getId(), cause);
        }
    }

    /**
     * Return Callback：消息无法路由到队列时的回调
     */
    @Override
    public void returnedMessage(ReturnedMessage returned) {
        Message message = returned.getMessage();
        log.warn("消息无法路由 | exchange={} | routingKey={} | replyCode={} | replyText={} | body={}",
                returned.getExchange(), returned.getRoutingKey(),
                returned.getReplyCode(), returned.getReplyText(),
                new String(message.getBody()));
    }
}
