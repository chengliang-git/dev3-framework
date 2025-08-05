package com.guanwei.tles.casetransfer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.annotation.CapSubscribe;
import com.guanwei.tles.casetransfer.entity.oracle.CaseInfoEntity;
import com.guanwei.tles.casetransfer.service.CaseTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 案件消息处理器
 * 使用 @CapSubscribe 注解实现订阅，参考 .NET Core CAP 组件的订阅方式
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CaseMessageHandler {

    private final CaseTransferService caseTransferService;
    private final ObjectMapper objectMapper; // 使用全局配置的ObjectMapper

    @Value("${cap.default-group:case-transfer-group}")
    private String messageGroup;

    @PostConstruct
    public void init() {
        log.info("案件消息处理器初始化完成，使用 @CapSubscribe 注解订阅消息");
        log.info("消息组: {}", messageGroup);
        log.info("使用全局配置的ObjectMapper，支持LocalDateTime处理");
    }

    /**
     * 处理案件立案消息
     */
    @CapSubscribe(value = "tles.case.filing", group = "case-transfer-group")
    public void handleCaseFiling(CapMessage capMessage) {
        try {
            log.info("收到案件立案消息: {}", capMessage.getId());

            CaseInfoEntity caseEntity = objectMapper.readValue(capMessage.getContent(), CaseInfoEntity.class);
            caseTransferService.handleCaseCreated(caseEntity);

            log.info("案件立案消息处理完成: {}", capMessage.getId());
        } catch (Exception e) {
            log.error("处理案件立案消息失败: {}", capMessage.getId(), e);
            throw new RuntimeException("处理案件立案消息失败", e);
        }
    }

    /**
     * 处理案件调查报告消息
     */
    @CapSubscribe(value = "tles.case-handling-opinion.finnal-review", group = "case-transfer-group")
    public void handleCaseHandleFinalReview(CapMessage capMessage) {
        try {
            // 解析消息内容
            CaseInfoEntity caseEntity = objectMapper.readValue(capMessage.getContent(), CaseInfoEntity.class);

            // 调用转存服务
            caseTransferService.handleCaseUpdated(caseEntity);

            log.info("案件调查报告消息处理完成: {}", capMessage.getId());
        } catch (Exception e) {
            log.error("处理案件调查报告消息失败: {}", capMessage.getId(), e);
            throw new RuntimeException("处理案件调查报告消息失败", e);
        }
    }

    /**
     * 处理案件违法信息录入消息
     */
    @CapSubscribe(value = "tles.case.case-illegal", group = "case-transfer-group")
    public void handleCaseIllegal(CapMessage capMessage) {
        try {
            log.info("收到案件违法信息录入消息: {}", capMessage.getId());
            log.info("CapMessage详情: id={}, name={}, group={}, content={}", 
                    capMessage.getId(), capMessage.getName(), capMessage.getGroup(), capMessage.getContent());

            // 解析消息内容
            CaseInfoEntity caseEntity = objectMapper.readValue(capMessage.getContent(), CaseInfoEntity.class);
            log.info("成功解析CaseEntity: caseId={}, caseNo={}, partyName={}", 
                    caseEntity.getCaseId(), caseEntity.getCaseNo(), caseEntity.getPartyName());

            // 直接调用同步方法，将案件数据同步到MongoDB
            caseTransferService.syncCaseToMongoDB(caseEntity.getCaseId());

            log.info("案件违法信息录入消息处理完成: {}", capMessage.getId());
        } catch (Exception e) {
            log.error("处理案件违法信息录入消息失败: {}", capMessage.getId(), e);
            throw new RuntimeException("处理案件违法信息录入消息失败", e);
        }
    }

    /**
     * 处理案件行政处罚决定消息
     */
    @CapSubscribe(value = "tles.case.admin-penalty-decision", group = "case-transfer-group")
    public void handleCaseAdminPenaltyDecision(CapMessage capMessage) {
        try {
            // 解析消息内容
            CaseInfoEntity caseEntity = objectMapper.readValue(capMessage.getContent(), CaseInfoEntity.class);

            // 调用转存服务
            caseTransferService.handleCaseUpdated(caseEntity);
        } catch (Exception e) {
            throw new RuntimeException("处理案件行政处罚决定消息失败", e);
        }
    }

    /**
     * 处理案件结案消息
     */
    @CapSubscribe(value = "tles.case.closed", group = "case-transfer-group")
    public void handleCaseClosed(CapMessage capMessage) {
        try {
            // 解析消息内容
            CaseInfoEntity caseEntity = objectMapper.readValue(capMessage.getContent(), CaseInfoEntity.class);

            // 调用转存服务
            caseTransferService.handleCaseUpdated(caseEntity);
        } catch (Exception e) {
            throw new RuntimeException("处理案件结案消息失败", e);
        }
    }

    /**
     * 处理案件撤销消息
     */
    @CapSubscribe(value = "tles.case.case-cancel", group = "case-transfer-group")
    public void handleCaseCanceled(CapMessage capMessage) {
        try {
            log.info("收到案件撤销消息: {}", capMessage.getId());

            // 解析消息内容
            CaseInfoEntity caseEntity = objectMapper.readValue(capMessage.getContent(), CaseInfoEntity.class);

            // 调用转存服务
            caseTransferService.handleCaseDeleted(caseEntity);

            log.info("案件撤销消息处理完成: {}", capMessage.getId());
        } catch (Exception e) {
            log.error("处理案件撤销消息失败: {}", capMessage.getId(), e);
            throw new RuntimeException("处理案件撤销消息失败", e);
        }
    }

    /**
     * RabbitMQ监听器 - 案件立案（备用方案）
     * 队列名：tles.case.filing.case-transfer-group
     */
    @RabbitListener(queues = "#{caseFilingQueue.name}")
    public void handleCaseFilingRabbitMQ(Message message) {
        try {
            String messageBody = new String(message.getBody());
            log.debug("RabbitMQ收到案件立案消息: {}", messageBody);

            CapMessage capMessage = objectMapper.readValue(messageBody, CapMessage.class);

            if ("tles.case.filing".equals(capMessage.getName())) {
                handleCaseFiling(capMessage);
            }
        } catch (Exception e) {
            log.error("RabbitMQ处理案件立案消息失败", e);
        }
    }
}