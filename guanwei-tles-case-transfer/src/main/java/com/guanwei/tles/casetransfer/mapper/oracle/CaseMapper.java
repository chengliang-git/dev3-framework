package com.guanwei.tles.casetransfer.mapper.oracle;

import com.guanwei.tles.casetransfer.entity.oracle.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Oracle案件数据查询Mapper
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Mapper
@Repository
public interface CaseMapper {

    /**
     * 根据案件ID查询案件信息
     * 
     * @param caseId 案件ID
     * @return 案件实体
     */
    CaseInfoEntity selectByCaseId(@Param("caseId") String caseId);

    /**
     * 根据案件编号查询案件信息
     * 
     * @param caseNo 案件编号
     * @return 案件实体
     */
    CaseInfoEntity selectByCaseNo(@Param("caseNo") String caseNo);

    /**
     * 根据当事人姓名查询案件列表
     * 
     * @param partyName 当事人姓名
     * @return 案件列表
     */
    List<CaseInfoEntity> selectByPartyName(@Param("partyName") String partyName);

    /**
     * 根据当事单位名称查询案件列表
     * 
     * @param companyName 当事单位名称
     * @return 案件列表
     */
    List<CaseInfoEntity> selectByCompanyName(@Param("companyName") String companyName);

    /**
     * 根据案件状态查询案件列表
     * 
     * @param state 案件状态
     * @return 案件列表
     */
    List<CaseInfoEntity> selectByState(@Param("state") Integer state);

    /**
     * 根据违法地点查询案件列表
     * 
     * @param illegalLocation 违法地点
     * @return 案件列表
     */
    List<CaseInfoEntity> selectByIllegalLocation(@Param("illegalLocation") String illegalLocation);

    /**
     * 根据立案时间范围查询案件列表
     * 
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 案件列表
     */
    List<CaseInfoEntity> selectByCaseFilingTimeBetween(@Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询所有案件信息
     * 
     * @return 案件列表
     */
    List<CaseInfoEntity> selectAll();

    /**
     * 根据ID查询案件信息
     * 
     * @param id 案件ID
     * @return 案件实体
     */
    CaseInfoEntity selectById(@Param("id") String id);

    /**
     * 统计案件数量
     * 
     * @return 案件数量
     */
    long count();

    /**
     * 保存案件信息
     * 
     * @param entity 案件实体
     * @return 影响行数
     */
    int insert(CaseInfoEntity entity);

    /**
     * 更新案件信息
     * 
     * @param entity 案件实体
     * @return 影响行数
     */
    int update(CaseInfoEntity entity);

    /**
     * 根据ID删除案件信息
     * 
     * @param id 案件ID
     * @return 影响行数
     */
    int deleteById(@Param("id") String id);

    /**
     * 根据案件ID查询案件当事人信息
     * 
     * @param caseId 案件ID
     * @return 当事人列表
     */
    List<CasePersonalEntity> selectPartiesByCaseId(@Param("caseId") String caseId);

    /**
     * 根据案件ID查询案件当事单位信息
     * 
     * @param caseId 案件ID
     * @return 当事单位列表
     */
    List<CaseCompanyEntity> selectCompaniesByCaseId(@Param("caseId") String caseId);

    /**
     * 根据案件ID查询案件附件信息
     * 
     * @param caseId 案件ID
     * @return 附件列表
     */
    List<CaseAttachmentEntity> selectAttachmentsByCaseId(@Param("caseId") String caseId);

    /**
     * 根据案件ID查询案件车辆信息
     * 
     * @param caseId 案件ID
     * @return 车辆列表
     */
    List<CaseVehicleEntity> selectVehiclesByCaseId(@Param("caseId") String caseId);

    /**
     * 根据案件ID查询案件处罚信息
     * 
     * @param caseId 案件ID
     * @return 处罚列表
     */
    List<CasePunishEntity> selectPunishesByCaseId(@Param("caseId") String caseId);

    /**
     * 根据案件ID查询案件执法人员信息
     * 
     * @param caseId 案件ID
     * @return 执法人员列表
     */
    List<CaseOfficerEntity> selectOfficersByCaseId(@Param("caseId") String caseId);
}