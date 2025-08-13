package com.guanwei.framework.workflow.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 工作流连线
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTransition {

    /**
     * 连线ID
     */
    private String id;

    /**
     * 连线名称
     */
    private String name;

    /**
     * 源节点ID
     */
    private String fromNodeId;

    /**
     * 目标节点ID
     */
    private String toNodeId;

    /**
     * 条件表达式
     */
    private String condition;
}
