package com.guanwei.tles.casetransfer.service;

import com.guanwei.framework.common.service.BaseMongoService;
import com.guanwei.tles.casetransfer.dto.CaseMessage;
import com.guanwei.tles.casetransfer.entity.Case;

/**
 * 案件转存服务接口
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
public interface CaseTransferService extends BaseMongoService<Case> {

    /**
     * 处理案件新增消息
     * 
     * @param caseMessage 案件消息
     */
    void handleCaseCreated(CaseMessage caseMessage);

    /**
     * 处理案件修改消息
     * 
     * @param caseMessage 案件消息
     */
    void handleCaseUpdated(CaseMessage caseMessage);

    /**
     * 处理案件删除消息
     * 
     * @param caseMessage 案件消息
     */
    void handleCaseDeleted(CaseMessage caseMessage);

    /**
     * 同步案件数据到MongoDB
     * 
     * @param caseId 案件ID
     */
    void syncCaseToMongoDB(String caseId);
} 