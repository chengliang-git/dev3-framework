package com.guanwei.framework.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

/**
 * 缓存工具类
 * 提供缓存操作的便捷方法
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Component
public class CacheUtils {

    private final CacheManager cacheManager;

    public CacheUtils(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * 获取缓存
     */
    public Cache getCache(String cacheName) {
        return cacheManager.getCache(cacheName);
    }

    /**
     * 获取缓存值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String cacheName, Object key, Class<T> type) {
        Cache cache = getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                return (T) wrapper.get();
            }
        }
        return null;
    }

    /**
     * 获取缓存值，如果不存在则通过loader加载
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String cacheName, Object key, Callable<T> loader) {
        Cache cache = getCache(cacheName);
        if (cache != null) {
            try {
                return (T) cache.get(key, loader);
            } catch (Exception e) {
                log.error("Error loading cache value for key: {}", key, e);
                return null;
            }
        }
        return null;
    }

    /**
     * 设置缓存值
     */
    public void put(String cacheName, Object key, Object value) {
        Cache cache = getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
            log.debug("Cache put: {} -> {} = {}", cacheName, key, value);
        }
    }

    /**
     * 删除缓存
     */
    public void evict(String cacheName, Object key) {
        Cache cache = getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.debug("Cache evict: {} -> {}", cacheName, key);
        }
    }

    /**
     * 清空缓存
     */
    public void clear(String cacheName) {
        Cache cache = getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.debug("Cache clear: {}", cacheName);
        }
    }

    /**
     * 检查缓存是否存在
     */
    public boolean exists(String cacheName, Object key) {
        Cache cache = getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            return wrapper != null;
        }
        return false;
    }

    /**
     * 获取缓存统计信息
     */
    public String getCacheStats(String cacheName) {
        Cache cache = getCache(cacheName);
        if (cache != null) {
            return String.format("Cache: %s, Native: %s", cacheName, cache.getNativeCache().getClass().getSimpleName());
        }
        return String.format("Cache: %s, Not Found", cacheName);
    }
} 