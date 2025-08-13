package com.guanwei.framework.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置
 * 提供多级缓存支持和缓存注解
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Redis缓存管理器
     */
    @Bean
    @ConditionalOnClass(RedisConnectionFactory.class)
    @ConditionalOnMissingBean(name = "redisCacheManager")
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();

        log.info("Redis cache manager initialized");
        return cacheManager;
    }

    /**
     * Caffeine本地缓存管理器
     */
    @Bean
    @ConditionalOnClass(Caffeine.class)
    @ConditionalOnMissingBean(name = "caffeineCacheManager")
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(1000)
                .recordStats());

        log.info("Caffeine cache manager initialized");
        return cacheManager;
    }

    /**
     * 内存缓存管理器（兜底）
     */
    @Bean
    @ConditionalOnMissingBean(name = "memoryCacheManager")
    public ConcurrentMapCacheManager memoryCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        log.info("Memory cache manager initialized");
        return cacheManager;
    }

    /**
     * 主缓存管理器
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "primaryCacheManager")
    public CacheManager primaryCacheManager() {
        // 优先使用Redis，如果没有则使用Caffeine，最后兜底使用内存
        try {
            return redisCacheManager(null);
        } catch (Exception e) {
            try {
                return caffeineCacheManager();
            } catch (Exception ex) {
                return memoryCacheManager();
            }
        }
    }
} 