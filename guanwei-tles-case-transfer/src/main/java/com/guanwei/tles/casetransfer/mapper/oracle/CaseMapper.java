package com.guanwei.tles.casetransfer.mapper.oracle;

import com.guanwei.tles.casetransfer.entity.oracle.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

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