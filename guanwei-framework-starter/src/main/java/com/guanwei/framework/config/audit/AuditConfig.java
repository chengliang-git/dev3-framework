package com.guanwei.framework.config.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 审计配置
 * 提供基础的审计日志功能
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "framework.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditConfig {

    /**
     * 审计服务
     */
    @Bean
    @ConditionalOnMissingBean
    public AuditService auditService() {
        log.info("Audit service initialized");
        return new AuditService();
    }
}

/**
 * 审计服务
 */
class AuditService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuditService.class);

    /**
     * 记录操作审计
     */
    public void recordOperation(String operation, String resource, String details, String userId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setOperation(operation);
        auditLog.setResource(resource);
        auditLog.setDetails(details);
        auditLog.setUserId(userId);
        auditLog.setTimestamp(System.currentTimeMillis());
        auditLog.setIpAddress("127.0.0.1"); // 简化实现

        log.info("Audit: {} - {} - {} - {}", operation, resource, userId, details);
    }

    /**
     * 记录登录审计
     */
    public void recordLogin(String userId, String ipAddress, boolean success, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setOperation("LOGIN");
        auditLog.setResource("AUTH");
        auditLog.setDetails(details);
        auditLog.setUserId(userId);
        auditLog.setTimestamp(System.currentTimeMillis());
        auditLog.setIpAddress(ipAddress);
        auditLog.setSuccess(success);

        log.info("Login audit: {} - {} - {} - {}", userId, ipAddress, success, details);
    }
}

/**
 * 审计日志实体
 */
class AuditLog {
    private String operation;
    private String resource;
    private String details;
    private String userId;
    private long timestamp;
    private String ipAddress;
    private boolean success = true;

    // Getters and Setters
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
