package com.guanwei.framework.workflow;

import lombok.extern.slf4j.Slf4j;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于Activiti7的工作流引擎实现
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Service
public class ActivitiWorkflowEngine implements WorkflowEngine {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Override
    public boolean deployWorkflow(com.guanwei.framework.workflow.model.WorkflowDefinition definition) {
        try {
            String resourceName = definition.getKey() + ".bpmn20.xml";
            InputStream bpmnStream = generateBpmnFromDefinition(definition);
            
            Deployment deployment = repositoryService.createDeployment()
                    .addInputStream(resourceName, bpmnStream)
                    .name(definition.getName())
                    .deploy();
            
            log.info("工作流部署成功: {}, 部署ID: {}", definition.getName(), deployment.getId());
            return true;
        } catch (Exception e) {
            log.error("工作流部署失败: {}", definition.getName(), e);
            return false;
        }
    }

    @Override
    public com.guanwei.framework.workflow.model.WorkflowInstance startWorkflow(String workflowKey, String businessKey, Map<String, Object> variables) {
        try {
            ProcessInstance processInstance = processRuntime.start(org.activiti.api.process.model.builders.ProcessPayloadBuilder
                    .start()
                    .withProcessDefinitionKey(workflowKey)
                    .withBusinessKey(businessKey)
                    .withVariables(variables)
                    .build());

            return convertToWorkflowInstance(processInstance);
        } catch (Exception e) {
            log.error("启动工作流失败: {}", workflowKey, e);
            return null;
        }
    }

    @Override
    public boolean completeTask(String taskId, String userId, Map<String, Object> variables) {
        try {
            taskRuntime.complete(TaskPayloadBuilder
                    .complete()
                    .withTaskId(taskId)
                    .withVariables(variables)
                    .build());
            return true;
        } catch (Exception e) {
            log.error("完成任务失败: {}", taskId, e);
            return false;
        }
    }

    @Override
    public List<com.guanwei.framework.workflow.model.WorkflowTask> getUserTasks(String userId) {
        try {
            Pageable pageable = Pageable.of(0, 1000);
            List<Task> tasks = taskRuntime.tasks(pageable, TaskPayloadBuilder.tasks().build())
                    .getContent()
                    .stream()
                    .filter(task -> userId.equals(task.getAssignee()))
                    .collect(Collectors.toList());

            return tasks.stream()
                    .map(this::convertToWorkflowTask)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户任务失败: {}", userId, e);
            return List.of();
        }
    }

    @Override
    public com.guanwei.framework.workflow.model.WorkflowInstance getWorkflowInstance(String instanceId) {
        try {
            org.activiti.engine.runtime.ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(instanceId)
                    .singleResult();
            
            if (processInstance != null) {
                return convertToWorkflowInstance(processInstance);
            }
            return null;
        } catch (Exception e) {
            log.error("获取工作流实例失败: {}", instanceId, e);
            return null;
        }
    }

    @Override
    public boolean suspendWorkflow(String instanceId) {
        try {
            runtimeService.suspendProcessInstanceById(instanceId);
            return true;
        } catch (Exception e) {
            log.error("挂起工作流实例失败: {}", instanceId, e);
            return false;
        }
    }

    @Override
    public boolean resumeWorkflow(String instanceId) {
        try {
            runtimeService.activateProcessInstanceById(instanceId);
            return true;
        } catch (Exception e) {
            log.error("恢复工作流实例失败: {}", instanceId, e);
            return false;
        }
    }

    @Override
    public boolean terminateWorkflow(String instanceId, String reason) {
        try {
            runtimeService.deleteProcessInstance(instanceId, reason);
            return true;
        } catch (Exception e) {
            log.error("终止工作流实例失败: {}", instanceId, e);
            return false;
        }
    }

    @Override
    public List<com.guanwei.framework.workflow.model.WorkflowTask> getWorkflowHistory(String instanceId) {
        try {
            List<org.activiti.engine.task.Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(instanceId)
                    .orderByTaskCreateTime()
                    .asc()
                    .list();

            return tasks.stream()
                    .map(this::convertToWorkflowTask)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取工作流历史失败: {}", instanceId, e);
            return List.of();
        }
    }

    private InputStream generateBpmnFromDefinition(com.guanwei.framework.workflow.model.WorkflowDefinition definition) {
        String bpmnXml = generateBasicBpmnXml(definition);
        return new java.io.ByteArrayInputStream(bpmnXml.getBytes());
    }

    private String generateBasicBpmnXml(com.guanwei.framework.workflow.model.WorkflowDefinition definition) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n");
        xml.append("             xmlns:activiti=\"http://activiti.org/bpmn\"\n");
        xml.append("             typeLanguage=\"http://www.w3.org/2001/XMLSchema\"\n");
        xml.append("             expressionLanguage=\"http://www.w3.org/1999/XPath\"\n");
        xml.append("             targetNamespace=\"http://www.activiti.org/processdef\">\n");
        
        xml.append("  <process id=\"").append(definition.getKey()).append("\" name=\"").append(definition.getName()).append("\">\n");
        xml.append("    <startEvent id=\"startEvent\" name=\"开始\"/>\n");
        
        if (definition.getNodes() != null) {
            for (com.guanwei.framework.workflow.model.WorkflowNode node : definition.getNodes()) {
                if (node.getType() == com.guanwei.framework.workflow.model.WorkflowNode.NodeType.TASK) {
                    xml.append("    <userTask id=\"").append(node.getId()).append("\" name=\"").append(node.getName()).append("\"/>\n");
                }
            }
        }
        
        xml.append("    <endEvent id=\"endEvent\" name=\"结束\"/>\n");
        
        if (definition.getTransitions() != null) {
            for (com.guanwei.framework.workflow.model.WorkflowTransition transition : definition.getTransitions()) {
                xml.append("    <sequenceFlow id=\"").append(transition.getId()).append("\" ");
                xml.append("sourceRef=\"").append(transition.getFromNodeId()).append("\" ");
                xml.append("targetRef=\"").append(transition.getToNodeId()).append("\"/>\n");
            }
        }
        
        xml.append("  </process>\n");
        xml.append("</definitions>");
        
        return xml.toString();
    }

    private com.guanwei.framework.workflow.model.WorkflowInstance convertToWorkflowInstance(ProcessInstance processInstance) {
        return com.guanwei.framework.workflow.model.WorkflowInstance.builder()
                .id(processInstance.getId())
                .workflowKey(processInstance.getProcessDefinitionKey())
                .businessKey(processInstance.getBusinessKey())
                .status(convertStatus(processInstance.getStatus()))
                .variables(new HashMap<>()) // 暂时使用空Map，等待getProcessVariables()方法可用
                .startTime(processInstance.getStartDate() != null ? 
                    processInstance.getStartDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .build();
    }

    private com.guanwei.framework.workflow.model.WorkflowInstance convertToWorkflowInstance(org.activiti.engine.runtime.ProcessInstance processInstance) {
        return com.guanwei.framework.workflow.model.WorkflowInstance.builder()
                .id(processInstance.getId())
                .workflowKey(processInstance.getProcessDefinitionKey())
                .businessKey(processInstance.getBusinessKey())
                .status(com.guanwei.framework.workflow.model.WorkflowInstance.InstanceStatus.RUNNING)
                .variables(new HashMap<>())
                .startTime(processInstance.getStartTime() != null ? 
                    processInstance.getStartTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .build();
    }

    private com.guanwei.framework.workflow.model.WorkflowTask convertToWorkflowTask(Task task) {
        return com.guanwei.framework.workflow.model.WorkflowTask.builder()
                .id(task.getId())
                .name(task.getName())
                .assignee(task.getAssignee())
                .processInstanceId(task.getProcessInstanceId())
                .createTime(task.getCreatedDate() != null ? 
                    task.getCreatedDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .build();
    }

    private com.guanwei.framework.workflow.model.WorkflowTask convertToWorkflowTask(org.activiti.engine.task.Task task) {
        return com.guanwei.framework.workflow.model.WorkflowTask.builder()
                .id(task.getId())
                .name(task.getName())
                .assignee(task.getAssignee())
                .processInstanceId(task.getProcessInstanceId())
                .createTime(task.getCreateTime() != null ? 
                    task.getCreateTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .build();
    }

    private com.guanwei.framework.workflow.model.WorkflowInstance.InstanceStatus convertStatus(ProcessInstance.ProcessInstanceStatus status) {
        switch (status) {
            case RUNNING:
                return com.guanwei.framework.workflow.model.WorkflowInstance.InstanceStatus.RUNNING;
            case SUSPENDED:
                return com.guanwei.framework.workflow.model.WorkflowInstance.InstanceStatus.SUSPENDED;
            case COMPLETED:
                return com.guanwei.framework.workflow.model.WorkflowInstance.InstanceStatus.COMPLETED;
            case CANCELLED:
                return com.guanwei.framework.workflow.model.WorkflowInstance.InstanceStatus.TERMINATED;
            default:
                return com.guanwei.framework.workflow.model.WorkflowInstance.InstanceStatus.RUNNING;
        }
    }
}
