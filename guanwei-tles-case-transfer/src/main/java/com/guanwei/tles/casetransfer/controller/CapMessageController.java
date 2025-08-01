package com.guanwei.tles.casetransfer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanwei.framework.cap.CapPublisher;
import com.guanwei.tles.casetransfer.dto.CaseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * CAP消息发布控制器
 * 用于测试消息发布功能
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/cap")
@RequiredArgsConstructor
public class CapMessageController {

    private final CapPublisher capPublisher;
    private final ObjectMapper objectMapper;

    /**
     * 发布案件新增消息
     */
    @PostMapping("/publish/case-created")
    public Map<String, Object> publishCaseCreated(@RequestBody CaseMessage caseMessage) {
        try {
            String messageId = capPublisher.publish("tles.case.filing", caseMessage, "case-transfer-group");

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("messageId", messageId);
            result.put("topic", "tles.case.filing");
            result.put("group", "case-transfer-group");
            result.put("timestamp", LocalDateTime.now());

            log.info("发布案件新增消息成功: {}", messageId);
            return result;
        } catch (Exception e) {
            log.error("发布案件新增消息失败", e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", LocalDateTime.now());

            return result;
        }
    }

    /**
     * 发布案件修改消息
     */
    @PostMapping("/publish/case-updated")
    public Map<String, Object> publishCaseUpdated(@RequestBody CaseMessage caseMessage) {
        try {
            String messageId = capPublisher.publish("case.updated", caseMessage, "case-transfer-group");

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("messageId", messageId);
            result.put("topic", "case.updated");
            result.put("group", "case-transfer-group");
            result.put("timestamp", LocalDateTime.now());

            log.info("发布案件修改消息成功: {}", messageId);
            return result;
        } catch (Exception e) {
            log.error("发布案件修改消息失败", e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", LocalDateTime.now());

            return result;
        }
    }

    /**
     * 发布案件删除消息
     */
    @PostMapping("/publish/case-deleted")
    public Map<String, Object> publishCaseDeleted(@RequestBody CaseMessage caseMessage) {
        try {
            String messageId = capPublisher.publish("case.deleted", caseMessage, "case-transfer-group");

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("messageId", messageId);
            result.put("topic", "case.deleted");
            result.put("group", "case-transfer-group");
            result.put("timestamp", LocalDateTime.now());

            log.info("发布案件删除消息成功: {}", messageId);
            return result;
        } catch (Exception e) {
            log.error("发布案件删除消息失败", e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", LocalDateTime.now());

            return result;
        }
    }

    /**
     * 发布延迟消息
     */
    @PostMapping("/publish/delay")
    public Map<String, Object> publishDelayMessage(@RequestBody Map<String, Object> request) {
        try {
            String topic = (String) request.get("topic");
            Object content = request.get("content");
            String group = (String) request.get("group");
            Long delaySeconds = Long.valueOf(request.get("delaySeconds").toString());

            String messageId = capPublisher.publishDelay(topic, content, group, delaySeconds);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("messageId", messageId);
            result.put("topic", topic);
            result.put("group", group);
            result.put("delaySeconds", delaySeconds);
            result.put("timestamp", LocalDateTime.now());

            log.info("发布延迟消息成功: {} (延迟{}秒)", messageId, delaySeconds);
            return result;
        } catch (Exception e) {
            log.error("发布延迟消息失败", e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", LocalDateTime.now());

            return result;
        }
    }

    /**
     * 获取CAP状态信息
     */
    @GetMapping("/status")
    public Map<String, Object> getCapStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", true);
        status.put("queueType", "rabbitmq");
        status.put("defaultGroup", "case-transfer-group");
        status.put("exchangeName", "cap.exchange");
        status.put("timestamp", LocalDateTime.now());

        return status;
    }
}