package com.guanwei.framework.cap;

import com.guanwei.framework.cap.queue.MemoryMessageQueue;
import com.guanwei.framework.cap.storage.MemoryMessageStorage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CapMemoryModeTest {

    @Test
    void publishAndConsumeInMemory() {
        CapProperties props = new CapProperties();
        props.setDefaultGroupName("test");

        var storage = new MemoryMessageStorage();
        var queue = new MemoryMessageQueue();
        var txMgr = new com.guanwei.framework.cap.impl.CapTransactionManagerImpl();

        var dispatcher = new com.guanwei.framework.cap.processor.DefaultMessageDispatcher(props, storage, queue, null, null);
        var publisher = new com.guanwei.framework.cap.impl.CapPublisherImpl(queue, storage, props, txMgr, dispatcher);
        var subscriber = new com.guanwei.framework.cap.impl.CapSubscriberImpl(storage, queue, props,
                new com.guanwei.framework.cap.queue.CapQueueManager(null, "ex", "topic", "test", "v1"));

        // 订阅
        final boolean[] consumed = {false};
        java.util.function.Consumer<CapMessage> handler = (msg) -> consumed[0] = true;
        subscriber.subscribe("demo", "test", handler);

        // 发布
        Long id = publisher.publish("demo", "hello", "test");
        assertNotNull(id);

        // 模拟消费
        subscriber.start();
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        assertTrue(consumed[0]);
        subscriber.stop();
    }
}


