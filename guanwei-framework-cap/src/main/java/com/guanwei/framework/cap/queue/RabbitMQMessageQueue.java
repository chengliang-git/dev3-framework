package com.guanwei.framework.cap.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapMessageStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * RabbitMQ 消息队列实现
 * 参考 GitHub CAP 源码的队列命名规则：routeKey + "." + groupName
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
public class RabbitMQMessageQueue implements MessageQueue {

    private final AmqpAdmin amqpAdmin;
    private final RabbitTemplate rabbitTemplate;
    private final ConnectionFactory connectionFactory;
    private final CapQueueManager capQueueManager;
    private final String exchangeName;
    private final String queuePrefix;

    private final ObjectMapper objectMapper;
    private final Map<String, SimpleMessageListenerContainer> containers = new ConcurrentHashMap<>();
    private final Map<String, List<CapMessage>> messageBuffers = new ConcurrentHashMap<>();

    public RabbitMQMessageQueue(AmqpAdmin amqpAdmin, RabbitTemplate rabbitTemplate,
            ConnectionFactory connectionFactory, CapQueueManager capQueueManager,
            String exchangeName, String queuePrefix) {
        this.amqpAdmin = amqpAdmin;
        this.rabbitTemplate = rabbitTemplate;
        this.connectionFactory = connectionFactory;
        this.capQueueManager = capQueueManager;
        this.exchangeName = exchangeName;
        this.queuePrefix = queuePrefix;

        // 配置ObjectMapper以支持多种日期时间格式
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 配置JavaTimeModule以支持多种日期时间格式
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 支持多种日期时间格式
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ISO_DATE_TIME, // 2025-07-02T10:49:00+08:00
                DateTimeFormatter.ISO_LOCAL_DATE_TIME, // 2025-07-02T10:49:00
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        };

        // 注册多种格式的序列化器和反序列化器
        for (DateTimeFormatter formatter : formatters) {
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        }

        this.objectMapper.registerModule(javaTimeModule);
    }

    @PostConstruct
    public void init() {
        log.info("CAP RabbitMQ MessageQueue initialized with exchange: {}", exchangeName);
        // 队列管理器的初始化已经在CapQueueManager中处理
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
            // 使用队列管理器确保队列存在并正确绑定
            String actualQueueName = null;
            if (capQueueManager != null) {
                actualQueueName = capQueueManager.createQueueAndBind(message.getName(), message.getGroup());
            } else {
                // 如果capQueueManager为null，使用默认的队列名称构建方式
                actualQueueName = message.getName() + "." + message.getGroup();
            }

            // 使用消息名称作为路由键
            String routingKey = message.getName();
            String messageJson = objectMapper.writeValueAsString(message);

            // 发送消息到交换机
            rabbitTemplate.convertAndSend(exchangeName, routingKey, messageJson);

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
            return send(queueName, message);
        } catch (Exception e) {
            log.error("Failed to send delay message to queue: {}", queueName, e);
            return false;
        }
    }

    @Override
    public CapMessage receive(String queueName, long timeout) {
        try {
            // 确保队列存在
            if (capQueueManager != null && !capQueueManager.queueExists(queueName)) {
                // 从队列名称中提取消息名称和组
                String messageName = capQueueManager.extractMessageTopicFromQueueName(queueName);
                String group = capQueueManager.extractGroupFromQueueName(queueName);
                if (messageName != null && group != null) {
                    capQueueManager.createQueueAndBind(messageName, group);
                } else {
                    log.error("Cannot extract message name and group from queue name: {}", queueName);
                    return null;
                }
            }

            // 从队列接收原始消息（二进制）
            org.springframework.amqp.core.Message message = rabbitTemplate.receive(queueName, timeout);
            if (message != null) {
                try {
                    // 获取消息体（二进制数据）
                    byte[] messageBody = message.getBody();

                    // 检查消息体是否为空
                    if (messageBody == null || messageBody.length == 0) {
                        log.debug("Received empty message from queue: {}", queueName);
                        return null;
                    }

                    // 尝试多种编码方式解析消息
                    String messageJson = null;
                    String[] encodings = { "UTF-8", "ISO-8859-1", "GBK" };

                    for (String encoding : encodings) {
                        try {
                            messageJson = new String(messageBody, encoding);
                            // 验证是否为有效的JSON
                            objectMapper.readTree(messageJson);
                            break;
                        } catch (Exception e) {
                            log.debug("Failed to decode message using encoding: {}", encoding);
                            continue;
                        }
                    }

                    if (messageJson == null) {
                        log.error("Failed to decode message from queue: {} with any encoding", queueName);
                        return null;
                    }

                    log.debug("Received message from queue {}: {}", queueName, messageJson);

                    // 尝试解析为CapMessage，如果失败则尝试解析为业务对象并转换
                    try {
                        // 首先尝试直接解析为CapMessage
                        CapMessage capMessage = objectMapper.readValue(messageJson, CapMessage.class);

                        // 检查解析出的CapMessage是否有效（有id和name字段）
                        if (capMessage.getId() != null && capMessage.getName() != null) {
                            log.debug("Successfully parsed as valid CapMessage: {}", capMessage.getId());
                            return capMessage;
                        } else {
                            log.debug("Parsed as CapMessage but fields are empty, treating as business object");
                            // 如果解析出的CapMessage字段为空，说明这可能是业务对象JSON
                            // 需要重新解析为业务对象并转换
                            throw new Exception("CapMessage fields are empty, treating as business object");
                        }
                    } catch (Exception e) {
                        log.debug("Failed to parse as valid CapMessage, trying to parse as business object: {}",
                                e.getMessage());

                        // 如果解析CapMessage失败，尝试解析为业务对象JSON并转换为CapMessage
                        try {
                            JsonNode jsonNode = objectMapper.readTree(messageJson);
                            if (jsonNode.isObject()) {
                                // 将业务对象JSON转换为CapMessage
                                CapMessage capMessage = convertBusinessObjectToCapMessage(jsonNode, queueName);
                                if (capMessage != null) {
                                    log.debug("Successfully converted business object to CapMessage: {}",
                                            capMessage.getId());
                                }
                                return capMessage;
                            } else if (jsonNode.isArray()) {
                                // 如果是数组，取第一个元素
                                if (jsonNode.size() > 0) {
                                    CapMessage capMessage = convertBusinessObjectToCapMessage(jsonNode.get(0),
                                            queueName);
                                    if (capMessage != null) {
                                        log.debug("Successfully converted array element to CapMessage: {}",
                                                capMessage.getId());
                                    }
                                    return capMessage;
                                }
                            }
                        } catch (Exception jsonException) {
                            log.error("Failed to parse message as either CapMessage or business object from queue: {}",
                                    queueName,
                                    jsonException);
                            return null;
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to parse message from queue: {}", queueName, e);
                    return null;
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to receive message from queue: {}", queueName, e);
            return null;
        }
    }

    /**
     * 将业务对象JSON转换为CapMessage
     * 适配.NET版CAP发送的业务对象格式
     */
    private CapMessage convertBusinessObjectToCapMessage(JsonNode jsonNode, String queueName) {
        try {
            log.debug("开始转换业务对象为CapMessage，队列名称: {}", queueName);

            // 从队列名称中提取消息主题
            String messageName = null;
            String group = null;

            if (capQueueManager != null) {
                messageName = capQueueManager.extractMessageTopicFromQueueName(queueName);
                group = capQueueManager.extractGroupFromQueueName(queueName);
                log.debug("使用capQueueManager解析队列名称: messageName={}, group={}", messageName, group);
            } else {
                // 如果capQueueManager为null，尝试从队列名称中手动解析
                if (queueName.contains(".")) {
                    int lastDotIndex = queueName.lastIndexOf(".");
                    messageName = queueName.substring(0, lastDotIndex);
                    group = queueName.substring(lastDotIndex + 1);
                } else {
                    messageName = queueName;
                    group = "default";
                }
                log.debug("手动解析队列名称: messageName={}, group={}", messageName, group);
            }

            // 创建CapMessage，将原始JSON作为content
            String content = jsonNode.toString();
            String messageId = java.util.UUID.randomUUID().toString();
            java.time.LocalDateTime now = java.time.LocalDateTime.now();

            log.debug("创建CapMessage: id={}, name={}, group={}, content长度={}",
                    messageId, messageName, group, content.length());

            CapMessage capMessage = CapMessage.builder()
                    .id(messageId)
                    .name(messageName != null ? messageName : "unknown")
                    .content(content) // 将业务对象JSON作为content
                    .group(group != null ? group : "default")
                    .status(CapMessageStatus.PENDING)
                    .retries(0)
                    .maxRetries(3)
                    .createdAt(now)
                    .updatedAt(now)
                    .sentTime(now)
                    .messageType(CapMessage.MessageType.NORMAL)
                    .build();

            // 初始化消息头
            capMessage.initializeHeaders();

            return capMessage;
        } catch (Exception e) {
            log.error("Failed to convert business object to CapMessage", e);
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

        if (!messages.isEmpty()) {
            log.debug("Received {} messages from queue: {}", messages.size(), queueName);
        }

        return messages;
    }

    @Override
    public boolean acknowledge(String queueName, String messageId) {
        // RabbitMQ的确认机制由监听器容器处理
        return true;
    }

    @Override
    public boolean reject(String queueName, String messageId, boolean requeue) {
        return true;
    }

    @Override
    public boolean deleteQueue(String queueName) {
        if (capQueueManager != null) {
            return capQueueManager.deleteQueue(queueName);
        }
        log.warn("CapQueueManager is null, cannot delete queue: {}", queueName);
        return false;
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
        if (capQueueManager != null) {
            return capQueueManager.purgeQueue(queueName);
        }
        log.warn("CapQueueManager is null, cannot clear queue: {}", queueName);
        return false;
    }

    @Override
    public boolean queueExists(String queueName) {
        if (capQueueManager != null) {
            return capQueueManager.queueExists(queueName);
        }
        log.warn("CapQueueManager is null, cannot check queue existence: {}", queueName);
        return false;
    }

    /**
     * 构建路由键
     * 参考 .NET CAP 源码：直接使用消息名称作为路由键
     * 例如：tles.case.filing -> tles.case.filing
     */
    private String buildRoutingKey(String messageName, String group) {
        return messageName;
    }

    /**
     * 构建队列名称
     * 参考 GitHub CAP 源码：routeKey + "." + groupName
     */
    public String buildQueueName(String messageName, String group) {
        if (capQueueManager != null) {
            return capQueueManager.buildQueueName(messageName, group);
        }
        // 如果capQueueManager为null，使用默认的队列名称构建方式
        return messageName + "." + group;
    }

    /**
     * 创建消息监听器容器
     * 参考 GitHub CAP 源码的消费者实现
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