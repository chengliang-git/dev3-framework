package com.guanwei.framework.workflow.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 工作流定义
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDefinition {

    /**
     * 工作流标识
     */
    private String key;

    /**
     * 工作流名称
     */
    private String name;

    /**
     * 工作流描述
     */
    private String description;

    /**
     * 版本号
     */
    private String version;

    /**
     * 流程节点定义
     */
    private List<WorkflowNode> nodes;

    /**
     * 流程连线定义
     */
    private List<WorkflowTransition> transitions;

    /**
     * 流程变量定义
     */
    private Map<String, Object> variables;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否启用
     */
    private boolean enabled = true;
}
