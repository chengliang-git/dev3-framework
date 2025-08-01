package com.guanwei.tles.casetransfer.dto;

import lombok.Data;

/**
 * 案件CAP消息实体类
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Data
public class CaseMessage {

    /**
     * 案件ID
     */
    private String caseId;

    /**
     * 操作类型（新增/修改/删除）
     */
    private String operationType;

    /**
     * 消息时间戳
     */
    private Long timestamp;

    /**
     * 消息来源
     */
    private String source;

    /**
     * 消息描述
     */
    private String description;
} 