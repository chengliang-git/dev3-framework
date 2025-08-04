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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 案件消息处理器
 * 支持内存队列和RabbitMQ两种模式
 * 参考 GitHub CAP 源码的队列命名规则：routeKey + "." + groupName
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

    @Value("${cap.default-group:case-transfer-group}")
    private String messageGroup;

    /**
     * 案件立案消息主题
     */
    private static final String CASE_FILING_TOPIC = "tles.case.filing";

    /**
     * 案件调查报告消息主题
     */
    private static final String CASE_HANDLE_FINAL_REVIEW = "tles.case-handling-opinion.finnal-review";

    /**
     * 案件违法信息录入消息主题
     */
    private static final String CASE_ILLEGAL = "tles.case.case-illegal";

    private static final String CASE_ADMIN_PENALTY_DECISION = "tles.case.admin-penalty-decision";

    private static final String CASE_CLOSED = "tles.case.closed";

    private static final String CASE_CANCELED = "tles.case.case-cancel";



    @PostConstruct
    public void init() {
        // 订阅案件立案消息
        capSubscriber.subscribe(CASE_FILING_TOPIC, messageGroup, this::handleCaseTransfer);

        // 订阅案件调查报告消息
        capSubscriber.subscribe(CASE_HANDLE_FINAL_REVIEW, messageGroup, this::handleCaseTransfer);

        // 订阅案件违法信息录入消息
        capSubscriber.subscribe(CASE_ILLEGAL, messageGroup, this::handleCaseTransfer);

        //订阅案件行政处罚决定
        capSubscriber.subscribe(CASE_ADMIN_PENALTY_DECISION, messageGroup, this::handleCaseTransfer);

        // 订阅案件删除消息
//        capSubscriber.subscribe(CASE_DELETED_TOPIC, messageGroup, this::handleCaseDeleted);
    }

    /**
     * 处理案件新增消息
     */
    public void handleCaseTransfer(CapMessage capMessage) {
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
     * 队列名：tles.case.filing.case-transfer-group
     */
    @RabbitListener(queues = "#{caseFilingQueue.name}")
    public void handleCaseCreatedRabbitMQ(Message message) {
        try {
            String messageBody = new String(message.getBody());
            log.debug("RabbitMQ收到案件新增消息: {}", messageBody);

            CapMessage capMessage = objectMapper.readValue(messageBody, CapMessage.class);

            if (CASE_FILING_TOPIC.equals(capMessage.getName())) {
                handleCaseTransfer(capMessage);
            }
        } catch (Exception e) {
            log.error("RabbitMQ处理案件传输消息失败", e);
        }
    }
}