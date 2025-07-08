package com.enterprise.framework.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 框架默认交换机
     */
    public static final String FRAMEWORK_EXCHANGE = "framework.exchange";

    /**
     * 框架默认队列
     */
    public static final String FRAMEWORK_QUEUE = "framework.queue";

    /**
     * 框架默认路由键
     */
    public static final String FRAMEWORK_ROUTING_KEY = "framework.routing.key";

    /**
     * 死信交换机
     */
    public static final String DEAD_LETTER_EXCHANGE = "framework.dead.letter.exchange";

    /**
     * 死信队列
     */
    public static final String DEAD_LETTER_QUEUE = "framework.dead.letter.queue";

    /**
     * 死信路由键
     */
    public static final String DEAD_LETTER_ROUTING_KEY = "framework.dead.letter.routing.key";

    /**
     * 消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        
        // 设置开启Mandatory,才能触发回调函数,无论消息推送结果怎么样都强制调用回调函数
        rabbitTemplate.setMandatory(true);
        
        // 消息确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("消息发送成功: " + correlationData);
            } else {
                System.out.println("消息发送失败: " + cause);
            }
        });
        
        // 消息返回回调
        rabbitTemplate.setReturnsCallback(returned -> {
            System.out.println("消息返回: " + returned.getMessage());
        });
        
        return rabbitTemplate;
    }

    /**
     * 监听器容器工厂
     */
    @Bean
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(1);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }

    /**
     * 声明框架交换机
     */
    @Bean
    public TopicExchange frameworkExchange() {
        return ExchangeBuilder.topicExchange(FRAMEWORK_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 声明框架队列
     */
    @Bean
    public Queue frameworkQueue() {
        return QueueBuilder.durable(FRAMEWORK_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .withArgument("x-message-ttl", 60000) // 消息TTL 60秒
                .build();
    }

    /**
     * 绑定框架队列到交换机
     */
    @Bean
    public Binding frameworkBinding() {
        return BindingBuilder.bind(frameworkQueue())
                .to(frameworkExchange())
                .with(FRAMEWORK_ROUTING_KEY);
    }

    /**
     * 声明死信交换机
     */
    @Bean
    public TopicExchange deadLetterExchange() {
        return ExchangeBuilder.topicExchange(DEAD_LETTER_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 声明死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    /**
     * 绑定死信队列到死信交换机
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }
}