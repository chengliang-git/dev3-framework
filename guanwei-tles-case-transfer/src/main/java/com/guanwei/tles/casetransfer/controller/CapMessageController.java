package com.guanwei.tles.casetransfer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapPublisher;
import com.guanwei.tles.casetransfer.dto.CaseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * CAP 消息测试控制器
 * 用于验证消息订阅和发送功能
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
     * 发送案件新增消息
     */
    @PostMapping("/send/case-created")
    public Map<String, Object> sendCaseCreated(@RequestBody CaseMessage caseMessage) {
        try {
            String messageId = capPublisher.publish("tles.case.filing", caseMessage, "case-transfer-group");

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("messageId", messageId);
            result.put("message", "案件新增消息发送成功");

            log.info("发送案件新增消息: {}", messageId);
            return result;
        } catch (Exception e) {
            log.error("发送案件新增消息失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 发送案件修改消息
     */
    @PostMapping("/send/case-updated")
    public Map<String, Object> sendCaseUpdated(@RequestBody CaseMessage caseMessage) {
        try {
            String messageId = capPublisher.publish("case.updated", caseMessage, "case-transfer-group");

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("messageId", messageId);
            result.put("message", "案件修改消息发送成功");

            log.info("发送案件修改消息: {}", messageId);
            return result;
        } catch (Exception e) {
            log.error("发送案件修改消息失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 发送案件违法信息录入消息
     */
    @PostMapping("/send/case-illegal")
    public Map<String, Object> sendCaseIllegal(@RequestBody Map<String, Object> caseData) {
        try {
            String messageId = capPublisher.publish("tles.case.case-illegal", caseData, "case-transfer-group");

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("messageId", messageId);
            result.put("message", "案件违法信息录入消息发送成功");

            log.info("发送案件违法信息录入消息: {}", messageId);
            return result;
        } catch (Exception e) {
            log.error("发送案件违法信息录入消息失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 发送案件删除消息
     */
    @PostMapping("/send/case-deleted")
    public Map<String, Object> sendCaseDeleted(@RequestBody CaseMessage caseMessage) {
        try {
            String messageId = capPublisher.publish("case.deleted", caseMessage, "case-transfer-group");

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("messageId", messageId);
            result.put("message", "案件删除消息发送成功");

            log.info("发送案件删除消息: {}", messageId);
            return result;
        } catch (Exception e) {
            log.error("发送案件删除消息失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 获取订阅状态
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "案件转存服务");
        status.put("timestamp", LocalDateTime.now());
        status.put("subscribedTopics", new String[] {
                "tles.case.filing",
                "tles.case-handling-opinion.finnal-review",
                "tles.case.case-illegal",
                "tles.case.admin-penalty-decision",
                "tles.case.closed",
                "tles.case.case-cancel"
        });
        status.put("messageGroup", "case-transfer-group");
        status.put("status", "running");

        return status;
    }
}