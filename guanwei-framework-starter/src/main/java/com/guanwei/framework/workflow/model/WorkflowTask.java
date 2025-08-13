package com.guanwei.framework.workflow.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作流任务
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTask {

    /**
     * 任务ID
     */
    private String id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务状态
     */
    private TaskStatus status;

    /**
     * 任务优先级
     */
    private int priority;

    /**
     * 任务指派人
     */
    private String assignee;

    /**
     * 任务候选人
     */
    private String candidateUser;

    /**
     * 任务候选组
     */
    private String candidateGroup;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 流程定义ID
     */
    private String processDefinitionId;

    /**
     * 任务创建时间
     */
    private LocalDateTime createTime;

    /**
     * 任务到期时间
     */
    private LocalDateTime dueDate;

    /**
     * 任务完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 任务表单Key
     */
    private String formKey;

    /**
     * 任务变量
     */
    private java.util.Map<String, Object> variables;

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING,    // 待处理
        IN_PROGRESS, // 处理中
        COMPLETED,  // 已完成
        CANCELLED,  // 已取消
        SUSPENDED   // 已挂起
    }
}
