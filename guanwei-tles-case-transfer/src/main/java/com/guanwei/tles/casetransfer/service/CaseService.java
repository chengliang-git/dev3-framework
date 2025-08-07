package com.guanwei.tles.casetransfer.service;

import com.guanwei.framework.common.entity.BaseEntity;
import com.guanwei.framework.common.service.BaseService;
import com.guanwei.tles.casetransfer.entity.oracle.CaseInfoEntity;

/**
 * 案件服务接口
 * 提供案件相关的业务操作
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
public interface CaseService extends BaseService<CaseInfoEntity> {

    /**
     * 根据案件ID查询案件信息
     * 
     * @param caseId 案件ID
     * @return 案件信息
     */
    CaseInfoEntity findByCaseId(String caseId);

    /**
     * 根据案件编号查询案件信息
     * 
     * @param caseNo 案件编号
     * @return 案件信息
     */
    CaseInfoEntity findByCaseNo(String caseNo);

    /**
     * 根据当事人姓名查询案件列表
     * 
     * @param partyName 当事人姓名
     * @return 案件列表
     */
    java.util.List<CaseInfoEntity> findByPartyName(String partyName);

    /**
     * 根据当事单位名称查询案件列表
     * 
     * @param companyName 当事单位名称
     * @return 案件列表
     */
    java.util.List<CaseInfoEntity> findByCompanyName(String companyName);

    /**
     * 根据案件状态查询案件列表
     * 
     * @param state 案件状态
     * @return 案件列表
     */
    java.util.List<CaseInfoEntity> findByState(Integer state);

    /**
     * 根据违法地点查询案件列表
     * 
     * @param illegalLocation 违法地点
     * @return 案件列表
     */
    java.util.List<CaseInfoEntity> findByIllegalLocation(String illegalLocation);

    /**
     * 根据立案时间范围查询案件列表
     * 
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 案件列表
     */
    java.util.List<CaseInfoEntity> findByCaseFilingTimeBetween(java.time.LocalDateTime startTime,
            java.time.LocalDateTime endTime);
}
