package com.guanwei.framework.cap.processor;

import com.guanwei.framework.cap.annotation.CapSubscribe;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * CAP 消费者执行描述符
 * 参考 .NET Core CAP 的 ConsumerExecutorDescriptor 类
 */
@Data
public class ConsumerExecutorDescriptor {

    /**
     * 实现类型信息
     */
    private Class<?> implTypeInfo;

    /**
     * 方法信息
     */
    private Method methodInfo;

    /**
     * 订阅属性
     */
    private CapSubscribe attribute;

    /**
     * 构造函数
     */
    public ConsumerExecutorDescriptor() {
    }

    /**
     * 构造函数
     *
     * @param implTypeInfo 实现类型信息
     * @param methodInfo   方法信息
     * @param attribute    订阅属性
     */
    public ConsumerExecutorDescriptor(Class<?> implTypeInfo, Method methodInfo, CapSubscribe attribute) {
        this.implTypeInfo = implTypeInfo;
        this.methodInfo = methodInfo;
        this.attribute = attribute;
    }

    /**
     * 获取主题名称
     *
     * @return 主题名称
     */
    public String getTopic() {
        return attribute != null ? attribute.value() : null;
    }

    /**
     * 获取组名称
     *
     * @return 组名称
     */
    public String getGroup() {
        return attribute != null ? attribute.group() : null;
    }
} 