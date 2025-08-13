package com.guanwei.framework.config.discovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务注册发现配置
 * 提供基础的服务注册发现功能
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "framework.discovery", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ServiceDiscoveryConfig {

    /**
     * 服务注册中心
     */
    @Bean
    @ConditionalOnMissingBean
    public ServiceRegistry serviceRegistry() {
        log.info("Service registry initialized");
        return new ServiceRegistry();
    }

    /**
     * 服务发现客户端
     */
    @Bean
    @ConditionalOnMissingBean
    public ServiceDiscoveryClient serviceDiscoveryClient(ServiceRegistry serviceRegistry) {
        log.info("Service discovery client initialized");
        return new ServiceDiscoveryClient(serviceRegistry);
    }
}

/**
 * 服务注册中心
 */
class ServiceRegistry {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ServiceRegistry.class);
    private final Map<String, ServiceInstance> serviceInstances = new ConcurrentHashMap<>();

    /**
     * 注册服务
     */
    public void registerService(ServiceInstance instance) {
        String serviceId = instance.getServiceId();
        serviceInstances.put(serviceId, instance);
        log.info("Service registered: {} -> {}", serviceId, instance.getHost() + ":" + instance.getPort());
    }

    /**
     * 注销服务
     */
    public void unregisterService(String serviceId) {
        ServiceInstance removed = serviceInstances.remove(serviceId);
        if (removed != null) {
            log.info("Service unregistered: {} -> {}", serviceId, removed.getHost() + ":" + removed.getPort());
        }
    }

    /**
     * 获取服务实例
     */
    public ServiceInstance getServiceInstance(String serviceId) {
        return serviceInstances.get(serviceId);
    }
}

/**
 * 服务发现客户端
 */
class ServiceDiscoveryClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ServiceDiscoveryClient.class);
    private final ServiceRegistry serviceRegistry;

    public ServiceDiscoveryClient(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * 发现服务
     */
    public ServiceInstance discoverService(String serviceId) {
        ServiceInstance instance = serviceRegistry.getServiceInstance(serviceId);
        if (instance != null && instance.getStatus() == ServiceStatus.UP) {
            log.debug("Service discovered: {} -> {}", serviceId, instance.getHost() + ":" + instance.getPort());
            return instance;
        }
        log.warn("Service not found or unavailable: {}", serviceId);
        return null;
    }
}

/**
 * 服务实例
 */
class ServiceInstance {
    private String serviceId;
    private String host;
    private int port;
    private ServiceStatus status = ServiceStatus.UP;

    // Getters and Setters
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    
    public ServiceStatus getStatus() { return status; }
    public void setStatus(ServiceStatus status) { this.status = status; }
}

/**
 * 服务状态枚举
 */
enum ServiceStatus {
    UP,        // 服务正常
    DOWN       // 服务不可用
}
