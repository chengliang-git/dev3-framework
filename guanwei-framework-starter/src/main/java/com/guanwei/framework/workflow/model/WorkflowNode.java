package com.guanwei.framework.workflow.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * 工作流节点
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowNode {

    /**
     * 节点ID
     */
    private String id;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 节点类型
     */
    private NodeType type;

    /**
     * 节点配置
     */
    private Map<String, Object> config;

    /**
     * 节点类型枚举
     */
    public enum NodeType {
        START,      // 开始节点
        END,        // 结束节点
        TASK,       // 任务节点
        GATEWAY,    // 网关节点
        SUBPROCESS  // 子流程节点
    }
}
