package com.guanwei.framework.cap.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanwei.framework.cap.CapMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * RabbitMQ 消息队列实现
 * 参考 .NET Core CAP 的队列命名规则：exchange + "." + group
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
@Component("rabbitMQMessageQueue")
public class RabbitMQMessageQueue implements MessageQueue {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Value("${cap.message-queue.exchange-name:cap.exchange}")
    private String exchangeName;

    @Value("${cap.message-queue.queue-prefix:cap_}")
    private String queuePrefix;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, SimpleMessageListenerContainer> containers = new ConcurrentHashMap<>();
    private final Map<String, List<CapMessage>> messageBuffers = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // 创建默认交换机
        Exchange exchange = new DirectExchange(exchangeName, true, false);
        amqpAdmin.declareExchange(exchange);
        log.info("CAP RabbitMQ exchange declared: {}", exchangeName);
    }

    @PreDestroy
    public void destroy() {
        // 关闭所有监听器容器
        containers.values().forEach(SimpleMessageListenerContainer::stop);
        containers.clear();
        log.info("CAP RabbitMQ message queue destroyed");
    }

    @Override
    public boolean send(String queueName, CapMessage message) {
        try {
            String routingKey = buildRoutingKey(message.getName(), message.getGroup());
            String messageJson = objectMapper.writeValueAsString(message);
            
            // 发送消息到交换机
            rabbitTemplate.convertAndSend(exchangeName, routingKey, messageJson);
            
            log.debug("Sent message to queue: {} with routing key: {}", queueName, routingKey);
            return true;
        } catch (Exception e) {
            log.error("Failed to send message to queue: {}", queueName, e);
            return false;
        }
    }

    @Override
    public CompletableFuture<Boolean> sendAsync(String queueName, CapMessage message) {
        return CompletableFuture.supplyAsync(() -> send(queueName, message));
    }

    @Override
    public boolean sendDelay(String queueName, CapMessage message, long delaySeconds) {
        try {
            // 延迟消息暂时使用内存队列实现，后续可以扩展为RabbitMQ的延迟队列
            log.warn("Delay message not fully implemented for RabbitMQ, using memory queue");
            return send(queueName, message);
        } catch (Exception e) {
            log.error("Failed to send delay message to queue: {}", queueName, e);
            return false;
        }
    }

    @Override
    public CapMessage receive(String queueName, long timeout) {
        try {
            // 从队列接收消息
            Object result = rabbitTemplate.receiveAndConvert(queueName, timeout);
            if (result != null) {
                return objectMapper.readValue(result.toString(), CapMessage.class);
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to receive message from queue: {}", queueName, e);
            return null;
        }
    }

    @Override
    public List<CapMessage> receiveBatch(String queueName, int maxCount, long timeout) {
        List<CapMessage> messages = new CopyOnWriteArrayList<>();
        long endTime = System.currentTimeMillis() + timeout;
        
        while (messages.size() < maxCount && System.currentTimeMillis() < endTime) {
            CapMessage message = receive(queueName, 100); // 100ms timeout for each message
            if (message != null) {
                messages.add(message);
            } else {
                break; // 没有更多消息
            }
        }
        
        return messages;
    }

    @Override
    public boolean acknowledge(String queueName, String messageId) {
        // RabbitMQ的确认机制由监听器容器处理
        log.debug("Message acknowledged: {} from queue: {}", messageId, queueName);
        return true;
    }

    @Override
    public boolean reject(String queueName, String messageId, boolean requeue) {
        // RabbitMQ的拒绝机制由监听器容器处理
        log.debug("Message rejected: {} from queue: {} (requeue: {})", messageId, queueName, requeue);
        return true;
    }

    /**
     * 创建队列（内部方法，用于初始化）
     */
    private boolean createQueue(String queueName) {
        try {
            // 创建队列
            Queue queue = new Queue(queueName, true, false, false);
            amqpAdmin.declareQueue(queue);
            
            // 绑定队列到交换机，使用队列名作为路由键
            Binding binding = new Binding(queueName, Binding.DestinationType.QUEUE, 
                    exchangeName, queueName, null);
            amqpAdmin.declareBinding(binding);
            
            log.info("Created CAP queue: {} and bound to exchange: {}", queueName, exchangeName);
            return true;
        } catch (Exception e) {
            log.error("Failed to create queue: {}", queueName, e);
            return false;
        }
    }

    @Override
    public boolean deleteQueue(String queueName) {
        try {
            amqpAdmin.deleteQueue(queueName);
            log.info("Deleted CAP queue: {}", queueName);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete queue: {}", queueName, e);
            return false;
        }
    }

    @Override
    public long getQueueLength(String queueName) {
        try {
            // 简化实现，返回0表示无法获取队列长度
            log.debug("Queue length not implemented for RabbitMQ: {}", queueName);
            return 0;
        } catch (Exception e) {
            log.error("Failed to get queue length for: {}", queueName, e);
            return 0;
        }
    }

    @Override
    public boolean clearQueue(String queueName) {
        try {
            // 清空队列中的所有消息
            amqpAdmin.purgeQueue(queueName, false);
            log.info("Cleared CAP queue: {}", queueName);
            return true;
        } catch (Exception e) {
            log.error("Failed to clear queue: {}", queueName, e);
            return false;
        }
    }

    @Override
    public boolean queueExists(String queueName) {
        try {
            // 简化实现，总是返回true
            log.debug("Queue existence check not implemented for RabbitMQ: {}", queueName);
            return true;
        } catch (Exception e) {
            log.error("Failed to check queue existence: {}", queueName, e);
            return false;
        }
    }

    /**
     * 构建路由键
     * 参考 .NET Core CAP 的命名规则：exchange + "." + group
     */
    private String buildRoutingKey(String messageName, String group) {
        return exchangeName + "." + group;
    }

    /**
     * 构建队列名称
     * 参考 .NET Core CAP 的命名规则：group 作为队列名
     */
    public String buildQueueName(String messageName, String group) {
        return group;
    }

    /**
     * 创建消息监听器容器
     * 参考 .NET Core CAP 的消费者实现
     */
    public SimpleMessageListenerContainer createMessageListenerContainer(String queueName, 
            MessageListenerAdapter messageListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(messageListenerAdapter);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setPrefetchCount(1);
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(1);
        
        containers.put(queueName, container);
        container.start();
        
        log.info("Created message listener container for queue: {}", queueName);
        return container;
    }
}