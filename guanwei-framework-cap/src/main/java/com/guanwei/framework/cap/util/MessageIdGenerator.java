package com.guanwei.framework.cap.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 消息ID生成器
 * 使用雪花算法生成唯一的消息ID
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
public class MessageIdGenerator {
    
    private static final long EPOCH = 1609459200000L; // 2021-01-01 00:00:00 UTC
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;
    
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    
    private final long workerId;
    private final long datacenterId;
    private final AtomicLong sequence = new AtomicLong(0L);
    private long lastTimestamp = -1L;
    
    private static volatile MessageIdGenerator instance;
    
    /**
     * 私有构造函数
     */
    private MessageIdGenerator() {
        // 使用当前进程ID作为workerId，线程ID作为datacenterId
        this.workerId = getProcessId() % (MAX_WORKER_ID + 1);
        this.datacenterId = getThreadId() % (MAX_DATACENTER_ID + 1);
        
        log.info("MessageIdGenerator initialized with workerId: {}, datacenterId: {}", workerId, datacenterId);
    }
    
    /**
     * 获取单例实例
     */
    public static MessageIdGenerator getInstance() {
        if (instance == null) {
            synchronized (MessageIdGenerator.class) {
                if (instance == null) {
                    instance = new MessageIdGenerator();
                }
            }
        }
        return instance;
    }
    
    /**
     * 生成下一个消息ID
     */
    public synchronized Long nextId() {
        long timestamp = System.currentTimeMillis();
        
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id for " + 
                    (lastTimestamp - timestamp) + " milliseconds");
        }
        
        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            long sequenceValue = sequence.incrementAndGet() & SEQUENCE_MASK;
            // 同一毫秒内序列数已经达到最大
            if (sequenceValue == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，毫秒内序列重置
            sequence.set(0L);
        }
        
        lastTimestamp = timestamp;
        
        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT) |
                (datacenterId << DATACENTER_ID_SHIFT) |
                (workerId << WORKER_ID_SHIFT) |
                sequence.get();
    }
    
    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
    
    /**
     * 获取当前进程ID
     */
    private long getProcessId() {
        try {
            String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
            return Long.parseLong(processName.split("@")[0]);
        } catch (Exception e) {
            return Thread.currentThread().getId();
        }
    }
    
    /**
     * 获取当前线程ID
     */
    private long getThreadId() {
        return Thread.currentThread().getId();
    }
} 