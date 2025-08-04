package com.guanwei.framework.cap;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CAP 配置属性
 * 参考 .NET Core CAP 组件的配置结构
 */
@Data
@ConfigurationProperties(prefix = "cap")
public class CapProperties {

    /**
     * 是否启用 CAP
     */
    private boolean enabled = true;

    /**
     * 默认消息组
     */
    private String defaultGroup = "default";

    /**
     * 消息存储配置
     */
    private Storage storage = new Storage();

    /**
     * 消息队列配置
     */
    private MessageQueue messageQueue = new MessageQueue();

    /**
     * 重试配置
     */
    private Retry retry = new Retry();

    /**
     * 消息存储配置
     */
    @Data
    public static class Storage {
        /**
         * 存储类型：memory, redis, database
         */
        private String type = "memory";

        /**
         * 数据库表前缀
         */
        private String tablePrefix = "cap_";

        /**
         * 消息过期时间（秒）
         */
        private long messageExpired = 24 * 60 * 60; // 24小时

        /**
         * 消息清理间隔（秒）
         */
        private long cleanupInterval = 60 * 60; // 1小时
    }

    /**
     * 消息队列配置
     */
    @Data
    public static class MessageQueue {
        /**
         * 队列类型：memory, rabbitmq, kafka
         */
        private String type = "memory";

        /**
         * 交换机名称（RabbitMQ专用）
         */
        private String exchangeName = "cap.exchange";

        /**
         * 交换机类型：direct, topic, fanout（RabbitMQ专用）
         */
        private String exchangeType = "topic";

        /**
         * 队列名称前缀
         */
        private String queuePrefix = "cap_";

        /**
         * 消费者线程池大小
         */
        private int consumerThreads = 10;

        /**
         * 消费者批处理大小
         */
        private int batchSize = 100;

        /**
         * 消费者轮询间隔（毫秒）
         */
        private long pollInterval = 1000;
    }

    /**
     * 重试配置
     */
    @Data
    public static class Retry {
        /**
         * 最大重试次数
         */
        private int maxRetries = 3;

        /**
         * 重试间隔（秒）
         */
        private long retryInterval = 60;

        /**
         * 重试策略：fixed, exponential
         */
        private String strategy = "fixed";

        /**
         * 指数退避的基数
         */
        private double exponentialBase = 2.0;
    }
}