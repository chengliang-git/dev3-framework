package com.guanwei.framework.workflow;

import com.guanwei.framework.workflow.model.WorkflowDefinition;
import com.guanwei.framework.workflow.model.WorkflowInstance;
import com.guanwei.framework.workflow.model.WorkflowTask;

import java.util.List;
import java.util.Map;

/**
 * 工作流引擎接口
 * 提供工作流定义、执行、管理等功能
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
public interface WorkflowEngine {

    /**
     * 部署工作流定义
     *
     * @param definition 工作流定义
     * @return 部署结果
     */
    boolean deployWorkflow(WorkflowDefinition definition);

    /**
     * 启动工作流实例
     *
     * @param workflowKey 工作流标识
     * @param businessKey 业务标识
     * @param variables 流程变量
     * @return 工作流实例
     */
    WorkflowInstance startWorkflow(String workflowKey, String businessKey, Map<String, Object> variables);

    /**
     * 完成任务
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @param variables 任务变量
     * @return 完成结果
     */
    boolean completeTask(String taskId, String userId, Map<String, Object> variables);

    /**
     * 获取用户待办任务
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    List<WorkflowTask> getUserTasks(String userId);

    /**
     * 获取工作流实例
     *
     * @param instanceId 实例ID
     * @return 工作流实例
     */
    WorkflowInstance getWorkflowInstance(String instanceId);

    /**
     * 挂起工作流实例
     *
     * @param instanceId 实例ID
     * @return 操作结果
     */
    boolean suspendWorkflow(String instanceId);

    /**
     * 恢复工作流实例
     *
     * @param instanceId 实例ID
     * @return 操作结果
     */
    boolean resumeWorkflow(String instanceId);

    /**
     * 终止工作流实例
     *
     * @param instanceId 实例ID
     * @param reason 终止原因
     * @return 操作结果
     */
    boolean terminateWorkflow(String instanceId, String reason);

    /**
     * 获取工作流历史
     *
     * @param instanceId 实例ID
     * @return 历史记录
     */
    List<WorkflowTask> getWorkflowHistory(String instanceId);
}
