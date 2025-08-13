package com.guanwei.framework.config.lock;

import com.guanwei.framework.config.FrameworkProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁配置
 * 提供基于Redis的分布式锁功能
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "framework.distributed-lock", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DistributedLockConfig {

    private final FrameworkProperties frameworkProperties;

    public DistributedLockConfig(FrameworkProperties frameworkProperties) {
        this.frameworkProperties = frameworkProperties;
    }

    /**
     * Redis模板配置
     */
    @Bean
    @ConditionalOnClass(RedisConnectionFactory.class)
    @ConditionalOnMissingBean(name = "distributedLockRedisTemplate")
    public RedisTemplate<String, String> distributedLockRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        
        log.info("Distributed lock Redis template initialized");
        return template;
    }

    /**
     * 分布式锁管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public DistributedLockManager distributedLockManager(RedisTemplate<String, String> redisTemplate) {
        log.info("Distributed lock manager initialized");
        return new DistributedLockManager(redisTemplate, frameworkProperties);
    }

    /**
     * 分布式锁服务
     */
    @Bean
    @ConditionalOnMissingBean
    public DistributedLockService distributedLockService(DistributedLockManager lockManager) {
        log.info("Distributed lock service initialized");
        return new DistributedLockService(lockManager);
    }
}

/**
 * 分布式锁管理器
 */
@Slf4j
class DistributedLockManager {

    private final RedisTemplate<String, String> redisTemplate;
    private final FrameworkProperties frameworkProperties;

    public DistributedLockManager(RedisTemplate<String, String> redisTemplate, FrameworkProperties frameworkProperties) {
        this.redisTemplate = redisTemplate;
        this.frameworkProperties = frameworkProperties;
    }

    /**
     * 尝试获取锁
     */
    public boolean tryLock(String lockKey, String lockValue, long timeout, TimeUnit timeUnit) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, timeout, timeUnit);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Failed to acquire lock: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 释放锁
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        try {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(script);
            redisScript.setResultType(Long.class);
            
            Long result = redisTemplate.execute(redisScript, 
                    java.util.Arrays.asList(lockKey), 
                    lockValue);
            return Long.valueOf(1).equals(result);
        } catch (Exception e) {
            log.error("Failed to release lock: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 检查锁是否存在
     */
    public boolean isLocked(String lockKey) {
        try {
            String value = redisTemplate.opsForValue().get(lockKey);
            return value != null;
        } catch (Exception e) {
            log.error("Failed to check lock status: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 获取锁的剩余时间
     */
    public long getLockRemainingTime(String lockKey) {
        try {
            Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.MILLISECONDS);
            return ttl != null ? ttl : -1;
        } catch (Exception e) {
            log.error("Failed to get lock remaining time: {}", lockKey, e);
            return -1;
        }
    }
}

/**
 * 分布式锁服务
 */
@Slf4j
class DistributedLockService {

    private final DistributedLockManager lockManager;

    public DistributedLockService(DistributedLockManager lockManager) {
        this.lockManager = lockManager;
    }

    /**
     * 执行带锁的操作
     */
    public <T> T executeWithLock(String lockKey, long timeout, TimeUnit timeUnit, LockableOperation<T> operation) {
        String lockValue = generateLockValue();
        
        try {
            // 尝试获取锁
            if (!lockManager.tryLock(lockKey, lockValue, timeout, timeUnit)) {
                throw new RuntimeException("Failed to acquire lock: " + lockKey);
            }
            
            log.debug("Lock acquired: {}", lockKey);
            
            // 执行操作
            try {
                return operation.execute();
            } catch (Exception e) {
                log.error("Operation execution failed: {}", lockKey, e);
                throw new RuntimeException("Operation execution failed", e);
            }
            
        } finally {
            // 释放锁
            if (lockManager.releaseLock(lockKey, lockValue)) {
                log.debug("Lock released: {}", lockKey);
            } else {
                log.warn("Failed to release lock: {}", lockKey);
            }
        }
    }

    /**
     * 执行带锁的操作（可重试）
     */
    public <T> T executeWithLockRetry(String lockKey, long timeout, TimeUnit timeUnit, 
                                     int maxRetries, long retryDelay, LockableOperation<T> operation) {
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts < maxRetries) {
            try {
                return executeWithLock(lockKey, timeout, timeUnit, operation);
            } catch (Exception e) {
                lastException = e;
                attempts++;
                
                if (attempts < maxRetries) {
                    log.warn("Lock operation failed, retrying... Attempt: {}/{}", attempts, maxRetries);
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Operation interrupted", ie);
                    }
                }
            }
        }
        
        throw new RuntimeException("Lock operation failed after " + maxRetries + " attempts", lastException);
    }

    /**
     * 生成锁值
     */
    private String generateLockValue() {
        return Thread.currentThread().getId() + ":" + System.currentTimeMillis();
    }

    /**
     * 可锁定操作的函数式接口
     */
    @FunctionalInterface
    public interface LockableOperation<T> {
        T execute() throws Exception;
    }
}
