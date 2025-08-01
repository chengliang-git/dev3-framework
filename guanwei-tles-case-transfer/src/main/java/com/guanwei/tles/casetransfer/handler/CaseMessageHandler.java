package com.guanwei.tles.casetransfer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapSubscriber;
import com.guanwei.tles.casetransfer.dto.CaseMessage;
import com.guanwei.tles.casetransfer.service.CaseTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 案件消息处理器
 * 支持内存队列和RabbitMQ两种模式
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CaseMessageHandler {

    private final CapSubscriber capSubscriber;
    private final CaseTransferService caseTransferService;
    private final ObjectMapper objectMapper;

    /**
     * 案件新增消息主题
     */
    private static final String CASE_FILING_TOPIC = "tles.case.filing";

    /**
     * 案件修改消息主题
     */
    private static final String CASE_UPDATED_TOPIC = "case.updated";

    /**
     * 案件删除消息主题
     */
    private static final String CASE_DELETED_TOPIC = "case.deleted";

    /**
     * 消息组名
     */
    private static final String MESSAGE_GROUP = "case-transfer-group";

    @PostConstruct
    public void init() {
        // 订阅案件新增消息
        capSubscriber.subscribe(CASE_FILING_TOPIC, MESSAGE_GROUP, this::handleCaseCreated);
        
        // 订阅案件修改消息
        capSubscriber.subscribe(CASE_UPDATED_TOPIC, MESSAGE_GROUP, this::handleCaseUpdated);
        
        // 订阅案件删除消息
        capSubscriber.subscribe(CASE_DELETED_TOPIC, MESSAGE_GROUP, this::handleCaseDeleted);
        
        log.info("案件消息处理器初始化完成");
    }

    /**
     * 处理案件新增消息
     */
    public void handleCaseCreated(CapMessage capMessage) {
        try {
            log.info("收到案件新增消息: {}", capMessage.getId());
            
            // 解析消息内容
            CaseMessage caseMessage = objectMapper.readValue(capMessage.getContent(), CaseMessage.class);
            
            // 调用转存服务
            caseTransferService.handleCaseCreated(caseMessage);
            
            log.info("案件新增消息处理完成: {}", capMessage.getId());
        } catch (Exception e) {
            log.error("处理案件新增消息失败: {}", capMessage.getId(), e);
            throw new RuntimeException("处理案件新增消息失败", e);
        }
    }

    /**
     * 处理案件修改消息
     */
    public void handleCaseUpdated(CapMessage capMessage) {
        try {
            log.info("收到案件修改消息: {}", capMessage.getId());
            
            // 解析消息内容
            CaseMessage caseMessage = objectMapper.readValue(capMessage.getContent(), CaseMessage.class);
            
            // 调用转存服务
            caseTransferService.handleCaseUpdated(caseMessage);
            
            log.info("案件修改消息处理完成: {}", capMessage.getId());
        } catch (Exception e) {
            log.error("处理案件修改消息失败: {}", capMessage.getId(), e);
            throw new RuntimeException("处理案件修改消息失败", e);
        }
    }

    /**
     * 处理案件删除消息
     */
    public void handleCaseDeleted(CapMessage capMessage) {
        try {
            log.info("收到案件删除消息: {}", capMessage.getId());
            
            // 解析消息内容
            CaseMessage caseMessage = objectMapper.readValue(capMessage.getContent(), CaseMessage.class);
            
            // 调用转存服务
            caseTransferService.handleCaseDeleted(caseMessage);
            
            log.info("案件删除消息处理完成: {}", capMessage.getId());
        } catch (Exception e) {
            log.error("处理案件删除消息失败: {}", capMessage.getId(), e);
            throw new RuntimeException("处理案件删除消息失败", e);
        }
    }

    /**
     * RabbitMQ监听器 - 案件新增
     */
    @RabbitListener(queues = "case-transfer-group")
    public void handleCaseCreatedRabbitMQ(Message message) {
        try {
            String messageBody = new String(message.getBody());
            CapMessage capMessage = objectMapper.readValue(messageBody, CapMessage.class);
            
            if (CASE_FILING_TOPIC.equals(capMessage.getName())) {
                handleCaseCreated(capMessage);
            }
        } catch (Exception e) {
            log.error("RabbitMQ处理案件新增消息失败", e);
        }
    }

    /**
     * RabbitMQ监听器 - 案件修改
     */
    @RabbitListener(queues = "case-transfer-group")
    public void handleCaseUpdatedRabbitMQ(Message message) {
        try {
            String messageBody = new String(message.getBody());
            CapMessage capMessage = objectMapper.readValue(messageBody, CapMessage.class);
            
            if (CASE_UPDATED_TOPIC.equals(capMessage.getName())) {
                handleCaseUpdated(capMessage);
            }
        } catch (Exception e) {
            log.error("RabbitMQ处理案件修改消息失败", e);
        }
    }

    /**
     * RabbitMQ监听器 - 案件删除
     */
    @RabbitListener(queues = "case-transfer-group")
    public void handleCaseDeletedRabbitMQ(Message message) {
        try {
            String messageBody = new String(message.getBody());
            CapMessage capMessage = objectMapper.readValue(messageBody, CapMessage.class);
            
            if (CASE_DELETED_TOPIC.equals(capMessage.getName())) {
                handleCaseDeleted(capMessage);
            }
        } catch (Exception e) {
            log.error("RabbitMQ处理案件删除消息失败", e);
        }
    }
} 