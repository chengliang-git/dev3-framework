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
     * 默认消息组名称
     */
    private String defaultGroupName = "cap.queue.default";

    /**
     * 消息组名称前缀
     */
    private String groupNamePrefix;

    /**
     * 主题名称前缀
     */
    private String topicNamePrefix;

    /**
     * 消息版本，用于在同一实例中隔离数据，长度不能超过20
     */
    private String version = "v1";

    /**
     * 成功消息过期时间（秒），默认24小时
     */
    private int succeedMessageExpiredAfter = 24 * 3600;

    /**
     * 失败消息过期时间（秒），默认15天
     */
    private int failedMessageExpiredAfter = 15 * 24 * 3600;

    /**
     * 失败重试间隔（秒），默认60秒
     */
    private int failedRetryInterval = 60;

    /**
     * 失败重试次数，默认50次
     */
    private int failedRetryCount = 50;

    /**
     * 消费者线程数量，默认1
     */
    private int consumerThreadCount = 1;

    /**
     * 是否启用订阅者并行执行，默认false
     */
    private boolean enableSubscriberParallelExecute = false;

    /**
     * 订阅者并行执行线程数量，默认CPU核心数
     */
    private int subscriberParallelExecuteThreadCount = Runtime.getRuntime().availableProcessors();

    /**
     * 订阅者并行执行缓冲区因子，默认1
     */
    private int subscriberParallelExecuteBufferFactor = 1;

    /**
     * 是否启用发布并行发送，默认false
     */
    private boolean enablePublishParallelSend = false;

    /**
     * 回退窗口回溯时间（秒），默认240秒
     */
    private int fallbackWindowLookbackSeconds = 240;

    /**
     * 收集器清理间隔（秒），默认300秒
     */
    private int collectorCleaningInterval = 300;

    /**
     * 调度器批处理大小，默认1000
     */
    private int schedulerBatchSize = 1000;

    /**
     * 是否使用存储锁，默认false
     */
    private boolean useStorageLock = false;

    /**
     * 消息存储配置
     */
    private Storage storage = new Storage();

    /**
     * 消息队列配置
     */
    private MessageQueue messageQueue = new MessageQueue();

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
         * 数据库连接配置
         */
        private Database database = new Database();

        /**
         * Redis连接配置
         */
        private Redis redis = new Redis();
    }

    /**
     * 数据库配置
     */
    @Data
    public static class Database {
        /**
         * 数据库类型：mysql, postgresql, sqlserver, oracle
         */
        private String type = "mysql";

        /**
         * 数据库连接URL
         */
        private String url;

        /**
         * 数据库用户名
         */
        private String username;

        /**
         * 数据库密码
         */
        private String password;

        /**
         * 数据库驱动类名
         */
        private String driverClassName;
    }

    /**
     * Redis配置
     */
    @Data
    public static class Redis {
        /**
         * Redis主机地址
         */
        private String host = "localhost";

        /**
         * Redis端口
         */
        private int port = 6379;

        /**
         * Redis密码
         */
        private String password;

        /**
         * Redis数据库索引
         */
        private int database = 0;

        /**
         * 连接超时时间（毫秒）
         */
        private int timeout = 2000;
    }

    /**
     * 消息队列配置
     */
    @Data
    public static class MessageQueue {
        /**
         * 队列类型：memory, rabbitmq, kafka, redis
         */
        private String type = "memory";

        /**
         * RabbitMQ配置
         */
        private RabbitMQ rabbitmq = new RabbitMQ();

        /**
         * Kafka配置
         */
        private Kafka kafka = new Kafka();
    }

    /**
     * RabbitMQ配置
     */
    @Data
    public static class RabbitMQ {
        /**
         * 交换机名称
         */
        private String exchangeName = "cap.exchange";

        /**
         * 交换机类型：direct, topic, fanout
         */
        private String exchangeType = "topic";

        /**
         * 队列名称前缀
         */
        private String queuePrefix = "cap_";

        /**
         * 主机地址
         */
        private String host = "localhost";

        /**
         * 端口
         */
        private int port = 5672;

        /**
         * 用户名
         */
        private String username = "guest";

        /**
         * 密码
         */
        private String password = "guest";

        /**
         * 虚拟主机
         */
        private String virtualHost = "/";
    }

    /**
     * Kafka配置
     */
    @Data
    public static class Kafka {
        /**
         * 服务器地址列表
         */
        private String bootstrapServers = "localhost:9092";

        /**
         * 消费者组ID
         */
        private String groupId;

        /**
         * 主题名称前缀
         */
        private String topicPrefix = "cap_";

        /**
         * 生产者配置
         */
        private Producer producer = new Producer();

        /**
         * 消费者配置
         */
        private Consumer consumer = new Consumer();
    }

    /**
     * Kafka生产者配置
     */
    @Data
    public static class Producer {
        /**
         * 确认机制：0, 1, all
         */
        private String acks = "all";

        /**
         * 重试次数
         */
        private int retries = 3;

        /**
         * 批处理大小
         */
        private int batchSize = 16384;

        /**
         * 等待时间（毫秒）
         */
        private int lingerMs = 1;
    }

    /**
     * Kafka消费者配置
     */
    @Data
    public static class Consumer {
        /**
         * 自动提交偏移量
         */
        private boolean enableAutoCommit = false;

        /**
         * 自动提交间隔（毫秒）
         */
        private int autoCommitIntervalMs = 1000;

        /**
         * 会话超时时间（毫秒）
         */
        private int sessionTimeoutMs = 30000;

        /**
         * 心跳间隔（毫秒）
         */
        private int heartbeatIntervalMs = 3000;
    }
}