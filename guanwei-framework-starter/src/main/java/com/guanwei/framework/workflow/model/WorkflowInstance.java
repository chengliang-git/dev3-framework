package com.guanwei.framework.workflow.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 工作流实例
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowInstance {

    /**
     * 实例ID
     */
    private String id;

    /**
     * 工作流定义标识
     */
    private String workflowKey;

    /**
     * 业务标识
     */
    private String businessKey;

    /**
     * 当前节点ID
     */
    private String currentNodeId;

    /**
     * 实例状态
     */
    private InstanceStatus status;

    /**
     * 流程变量
     */
    private Map<String, Object> variables;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 实例状态枚举
     */
    public enum InstanceStatus {
        RUNNING,    // 运行中
        SUSPENDED,  // 已挂起
        COMPLETED,  // 已完成
        TERMINATED  // 已终止
    }
}
