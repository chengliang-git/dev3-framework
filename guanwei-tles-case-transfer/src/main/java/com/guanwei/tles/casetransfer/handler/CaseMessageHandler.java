package com.guanwei.tles.casetransfer.handler;

import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapSubscriber;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanwei.tles.casetransfer.dto.CaseMessage;
import com.guanwei.tles.casetransfer.service.CaseTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 案件消息处理器
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
        log.info("收到案件新增消息: {}", capMessage);
        try {
            CaseMessage caseMessage = parseCaseMessage(capMessage);
            caseTransferService.handleCaseCreated(caseMessage);
        } catch (Exception e) {
            log.error("处理案件新增消息失败: {}", capMessage, e);
            throw e;
        }
    }

    /**
     * 处理案件修改消息
     */
    public void handleCaseUpdated(CapMessage capMessage) {
        log.info("收到案件修改消息: {}", capMessage);
        try {
            CaseMessage caseMessage = parseCaseMessage(capMessage);
            caseTransferService.handleCaseUpdated(caseMessage);
        } catch (Exception e) {
            log.error("处理案件修改消息失败: {}", capMessage, e);
            throw e;
        }
    }

    /**
     * 处理案件删除消息
     */
    public void handleCaseDeleted(CapMessage capMessage) {
        log.info("收到案件删除消息: {}", capMessage);
        try {
            CaseMessage caseMessage = parseCaseMessage(capMessage);
            caseTransferService.handleCaseDeleted(caseMessage);
        } catch (Exception e) {
            log.error("处理案件删除消息失败: {}", capMessage, e);
            throw e;
        }
    }

    /**
     * 解析案件消息
     */
    private CaseMessage parseCaseMessage(CapMessage capMessage) {
        try {
            return objectMapper.readValue(capMessage.getContent(), CaseMessage.class);
        } catch (Exception e) {
            log.error("解析案件消息失败: {}", capMessage.getContent(), e);
            throw new RuntimeException("解析案件消息失败", e);
        }
    }
} 