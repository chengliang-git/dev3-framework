package com.guanwei.framework.cap.storage;

/**
 * 幂等去重存储
 * 尝试以原子方式标记 key 已处理，如已存在则返回 false
 */
public interface DedupStorage {
    /**
     * 尝试标记已处理
     * @param key 唯一幂等键（建议使用消息ID或业务幂等键）
     * @param ttlSeconds 过期秒数（用于限制去重窗口），<=0 表示不过期
     * @return true 表示首次处理，false 表示已处理（重复）
     */
    boolean tryMarkProcessed(String key, long ttlSeconds);
}


