package com.guanwei.framework.cap.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CAP 队列管理器
 * 负责队列的创建、绑定和管理
 * 参考 GitHub CAP 源码的队列管理逻辑
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
public class CapQueueManager {

    private final RabbitAdmin rabbitAdmin;
    private final String exchangeName;
    private final String exchangeType;
    private final String defaultGroup;

    public CapQueueManager(RabbitAdmin rabbitAdmin, String exchangeName, String exchangeType, String defaultGroup) {
        this.rabbitAdmin = rabbitAdmin;
        this.exchangeName = exchangeName;
        this.exchangeType = exchangeType;
        this.defaultGroup = defaultGroup;
    }

    // 缓存已创建的队列，避免重复创建
    private final Map<String, Boolean> createdQueues = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("CAP Queue Manager initialized with exchange: {}", exchangeName);
        try {
            // 确保交换机存在
            ensureExchangeExists();
        } catch (Exception e) {
            log.error("Failed to initialize CAP Queue Manager", e);
        }
    }

    /**
     * 确保交换机存在
     */
    private void ensureExchangeExists() {
        try {
            // 先检查交换机是否已经存在
            if (exchangeExists()) {
                log.debug("CAP exchange already exists: {} (type: {})", exchangeName, exchangeType);
                return;
            }

            Exchange exchange;
            switch (exchangeType.toLowerCase()) {
                case "direct":
                    exchange = new DirectExchange(exchangeName, true, false);
                    break;
                case "fanout":
                    exchange = new FanoutExchange(exchangeName, true, false);
                    break;
                case "topic":
                default:
                    exchange = new TopicExchange(exchangeName, true, false);
                    break;
            }
            rabbitAdmin.declareExchange(exchange);
            log.info("CAP exchange declared: {} (type: {})", exchangeName, exchangeType);
        } catch (Exception e) {
            log.error("Failed to declare CAP exchange: {} (type: {})", exchangeName, exchangeType, e);
            throw new RuntimeException("Failed to declare CAP exchange", e);
        }
    }

    /**
     * 检查交换机是否存在
     * 
     * @return 是否存在
     */
    private boolean exchangeExists() {
        try {
            // 尝试声明一个相同类型的交换机，如果已存在且类型相同则不会报错
            Exchange exchange;
            switch (exchangeType.toLowerCase()) {
                case "direct":
                    exchange = new DirectExchange(exchangeName, true, false);
                    break;
                case "fanout":
                    exchange = new FanoutExchange(exchangeName, true, false);
                    break;
                case "topic":
                default:
                    exchange = new TopicExchange(exchangeName, true, false);
                    break;
            }
            rabbitAdmin.declareExchange(exchange);
            return true;
        } catch (Exception e) {
            // 如果交换机已存在但类型不同，会抛出异常
            log.debug("Exchange check failed for: {} (type: {})", exchangeName, exchangeType);
            return false;
        }
    }

    /**
     * 创建队列并绑定到交换机
     * 参考 GitHub CAP 源码：队列绑定使用消息主题作为路由键
     * 
     * @param messageName 消息主题
     * @param group       消息组
     * @return 队列名称
     */
    public String createQueueAndBind(String messageName, String group) {
        String queueName = buildQueueName(messageName, group);
        String cacheKey = queueName + ":" + messageName;

        // 检查是否已经创建过
        if (createdQueues.containsKey(cacheKey)) {
            log.debug("Queue already created: {} with routing key: {}", queueName, messageName);
            return queueName;
        }

        try {
            // 创建队列
            Queue queue = new Queue(queueName, true, false, false);
            rabbitAdmin.declareQueue(queue);

            // 绑定队列到交换机，使用消息主题作为路由键
            Binding binding = new Binding(queueName, Binding.DestinationType.QUEUE,
                    exchangeName, messageName, null);
            rabbitAdmin.declareBinding(binding);

            // 标记为已创建
            createdQueues.put(cacheKey, true);

            log.info("Created CAP queue: {} and bound to exchange: {} with routing key: {}",
                    queueName, exchangeName, messageName);

            return queueName;
        } catch (Exception e) {
            log.error("Failed to create queue: {} with routing key: {}", queueName, messageName, e);
            throw new RuntimeException("Failed to create CAP queue", e);
        }
    }

    /**
     * 创建队列并绑定到交换机（使用默认组）
     * 
     * @param messageName 消息主题
     * @return 队列名称
     */
    public String createQueueAndBind(String messageName) {
        return createQueueAndBind(messageName, defaultGroup);
    }

    /**
     * 检查队列是否存在
     * 
     * @param queueName 队列名称
     * @return 是否存在
     */
    public boolean queueExists(String queueName) {
        try {
            // 通过查询队列信息来检查是否存在
            Properties queueProperties = rabbitAdmin.getQueueProperties(queueName);
            return queueProperties != null;
        } catch (Exception e) {
            log.debug("Queue does not exist: {}", queueName);
            return false;
        }
    }

    /**
     * 删除队列
     * 
     * @param queueName 队列名称
     * @return 是否删除成功
     */
    public boolean deleteQueue(String queueName) {
        try {
            rabbitAdmin.deleteQueue(queueName);
            log.info("Deleted CAP queue: {}", queueName);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete queue: {}", queueName, e);
            return false;
        }
    }

    /**
     * 清空队列
     * 
     * @param queueName 队列名称
     * @return 是否清空成功
     */
    public boolean purgeQueue(String queueName) {
        try {
            rabbitAdmin.purgeQueue(queueName, false);
            log.info("Purged CAP queue: {}", queueName);
            return true;
        } catch (Exception e) {
            log.error("Failed to purge queue: {}", queueName, e);
            return false;
        }
    }

    /**
     * 构建队列名称
     * 参考 GitHub CAP 源码：routeKey + "." + groupName
     * 
     * @param messageName 消息主题
     * @param group       消息组
     * @return 队列名称
     */
    public String buildQueueName(String messageName, String group) {
        return messageName + "." + group;
    }

    /**
     * 从队列名中提取消息主题
     * 
     * @param queueName 队列名称
     * @return 消息主题
     */
    public String extractMessageTopicFromQueueName(String queueName) {
        if (queueName.contains(".")) {
            // 取第一个点之前的部分作为消息主题
            return queueName.substring(0, queueName.indexOf("."));
        }
        // 如果队列名不包含点，则使用队列名作为消息主题
        return queueName;
    }

    /**
     * 从队列名中提取消息组
     * 
     * @param queueName 队列名称
     * @return 消息组
     */
    public String extractGroupFromQueueName(String queueName) {
        if (queueName.contains(".")) {
            // 取第一个点之后的部分作为消息组
            return queueName.substring(queueName.indexOf(".") + 1);
        }
        // 如果队列名不包含点，则使用默认组
        return defaultGroup;
    }

    /**
     * 获取交换机名称
     * 
     * @return 交换机名称
     */
    public String getExchangeName() {
        return exchangeName;
    }

    /**
     * 获取默认组
     * 
     * @return 默认组
     */
    public String getDefaultGroup() {
        return defaultGroup;
    }
}