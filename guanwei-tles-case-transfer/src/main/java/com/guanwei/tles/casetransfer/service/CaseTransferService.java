package com.guanwei.tles.casetransfer.service;

// import com.guanwei.framework.common.service.BaseMongoService; // 暂时禁用MongoDB
//import com.guanwei.tles.casetransfer.entity.Case;
import com.guanwei.tles.casetransfer.entity.oracle.CaseInfoEntity;

/**
 * 案件转存服务接口 - MongoDB功能暂时禁用
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
public interface CaseTransferService {

    /**
     * 处理案件新增消息
     * 
     * @param caseEntity 案件消息
     */
    void handleCaseCreated(CaseInfoEntity caseEntity);

    /**
     * 处理案件修改消息
     * 
     * @param caseEntity 案件消息
     */
    void handleCaseUpdated(CaseInfoEntity caseEntity);

    /**
     * 处理案件删除消息
     * 
     * @param caseEntity 案件消息
     */
    void handleCaseDeleted(CaseInfoEntity caseEntity);

    /**
     * 同步案件数据到MongoDB（暂时禁用）
     * 
     * @param caseId 案件ID
     */
    void syncCaseToMongoDB(String caseId);
}