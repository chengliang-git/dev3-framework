package com.guanwei.framework.config.audit;

// import com.guanwei.framework.common.annotation.Audit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 审计拦截器
 * 自动拦截带有@Audit注解的方法，记录审计日志
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
public class AuditInterceptor {

    private final AuditService auditService;

    @Autowired
    public AuditInterceptor(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * 环绕通知，拦截带有@Audit注解的方法
     * 暂时注释掉，等待@Audit注解可用
     */
    /*
    @Around("@annotation(com.guanwei.framework.common.annotation.Audit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getSignature();
        Audit auditAnnotation = method.getAnnotation(Audit.class);

        // 获取审计信息
        String operation = getOperation(auditAnnotation, method);
        String resource = getResource(auditAnnotation, method);
        String description = getDescription(auditAnnotation, method);
        String userId = getCurrentUserId();

        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            // 执行原方法
            Object result = joinPoint.proceed();
            success = true;
            return result;

        } catch (Exception e) {
            success = false;
            throw e;

        } finally {
            // 记录操作审计
            long duration = System.currentTimeMillis() - startTime;
            String details = String.format("%s completed in %dms, success: %s", description, duration, success);
            auditService.recordOperation(operation, resource, details, userId);
        }
    }
    */

    /**
     * 获取操作类型
     * 暂时注释掉，等待@Audit注解可用
     */
    /*
    private String getOperation(Audit auditAnnotation, Method method) {
        String operation = auditAnnotation.operation();
        if (operation.isEmpty()) {
            // 根据方法名推断操作类型
            String methodName = method.getName().toUpperCase();
            if (methodName.startsWith("GET") || methodName.startsWith("FIND") || methodName.startsWith("QUERY")) {
                operation = "QUERY";
            } else if (methodName.startsWith("SAVE") || methodName.startsWith("INSERT") || methodName.startsWith("CREATE")) {
                operation = "CREATE";
            } else if (methodName.startsWith("UPDATE") || methodName.startsWith("MODIFY")) {
                operation = "UPDATE";
            } else if (methodName.startsWith("DELETE") || methodName.startsWith("REMOVE")) {
                operation = "DELETE";
            } else {
                operation = "EXECUTE";
            }
        }
        return operation;
    }
    */

    /**
     * 获取资源类型
     * 暂时注释掉，等待@Audit注解可用
     */
    /*
    private String getResource(Audit auditAnnotation, Method method) {
        String resource = auditAnnotation.resource();
        if (resource.isEmpty()) {
            // 根据类名推断资源类型
            String className = method.getDeclaringClass().getSimpleName();
            resource = className.replace("Controller", "").replace("Service", "").replace("Manager", "");
        }
        return resource;
    }
    */

    /**
     * 获取操作描述
     * 暂时注释掉，等待@Audit注解可用
     */
    /*
    private String getDescription(Audit auditAnnotation, Method method) {
        String description = auditAnnotation.description();
        if (description.isEmpty()) {
            description = method.getName();
        }
        return description;
    }
    */

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.warn("Failed to get current user ID", e);
        }
        return "anonymous";
    }
}
