package com.guanwei.tles.casetransfer.service.impl;

import com.guanwei.framework.common.exception.BusinessException;
import com.guanwei.framework.common.result.ResultCode;
// import com.guanwei.framework.common.service.impl.BaseMongoServiceImpl; // 暂时禁用MongoDB
//import com.guanwei.tles.casetransfer.entity.Case;
import com.guanwei.tles.casetransfer.entity.CaseDocument;
import com.guanwei.tles.casetransfer.entity.CaseParty;
import com.guanwei.tles.casetransfer.entity.oracle.*;
import com.guanwei.tles.casetransfer.mapper.oracle.CaseMapper;
// import com.guanwei.tles.casetransfer.repository.CaseRepository; // 暂时禁用MongoDB
import com.guanwei.tles.casetransfer.service.CaseTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 案件转存服务实现类
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseTransferServiceImpl implements CaseTransferService {

    // private final CaseRepository caseRepository; // 暂时禁用MongoDB Repository
    private final CaseMapper caseMapper;

    @Override
    @Transactional
    public void handleCaseCreated(CaseInfoEntity caseEntity) {
        log.info("处理案件新增消息: {} (MongoDB操作已暂时禁用)", caseEntity);
        try {
            // 暂时禁用MongoDB同步
            // syncCaseToMongoDB(caseEntity.getCaseId());
            log.info("案件新增处理完成: {} (MongoDB操作已暂时禁用)", caseEntity.getCaseId());
        } catch (Exception e) {
            log.error("处理案件新增消息失败: {}", caseEntity.getCaseId(), e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "处理案件新增消息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handleCaseUpdated(CaseInfoEntity caseEntity) {
        log.info("处理案件修改消息: {} (MongoDB操作已暂时禁用)", caseEntity);
        try {
            // 暂时禁用MongoDB同步
            // syncCaseToMongoDB(caseEntity.getCaseId());
            log.info("案件修改处理完成: {} (MongoDB操作已暂时禁用)", caseEntity.getCaseId());
        } catch (Exception e) {
            log.error("处理案件修改消息失败: {}", caseEntity.getCaseId(), e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "处理案件修改消息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handleCaseDeleted(CaseInfoEntity caseEntity) {
        log.info("处理案件删除消息: {} (MongoDB操作已暂时禁用)", caseEntity);
        try {
            // 暂时禁用MongoDB删除
            // caseRepository.deleteByCaseId(caseEntity.getCaseId());
            log.info("案件删除处理完成: {} (MongoDB操作已暂时禁用)", caseEntity.getCaseId());
        } catch (Exception e) {
            log.error("处理案件删除消息失败: {}", caseEntity.getCaseId(), e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "处理案件删除消息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void syncCaseToMongoDB(String caseId) {
        log.info("开始同步案件数据到MongoDB: {} (MongoDB操作已暂时禁用)", caseId);

        try {
            // 1. 从Oracle查询案件主表数据
            CaseInfoEntity caseInfoEntity = caseMapper.selectByCaseId(caseId);
            if (caseInfoEntity == null) {
                log.warn("案件不存在: {}", caseId);
                return;
            }

            // 2. 从Oracle查询案件当事人数据
            List<CasePersonalEntity> personalEntities = caseMapper.selectPartiesByCaseId(caseId);

            // 3. 从Oracle查询案件当事单位数据
            List<CaseCompanyEntity> companyEntities = caseMapper.selectCompaniesByCaseId(caseId);

            // 4. 从Oracle查询案件附件数据
//            List<CaseAttachmentEntity> attachmentEntities = caseMapper.selectAttachmentsByCaseId(caseId);

            // 5. 从Oracle查询案件车辆数据
            List<CaseVehicleEntity> vehicleEntities = caseMapper.selectVehiclesByCaseId(caseId);

            // 6. 从Oracle查询案件处罚数据
            List<CasePunishEntity> punishEntities = caseMapper.selectPunishesByCaseId(caseId);

            // 7. 从Oracle查询案件执法人员数据
            List<CaseOfficerEntity> officerEntities = caseMapper.selectOfficersByCaseId(caseId);

            // 8. 转换为MongoDB实体
//            Case mongoCase = convertToMongoCase(caseInfoEntity, personalEntities, companyEntities,
//                    null, vehicleEntities, punishEntities, officerEntities);

            // 9. 暂时禁用保存到MongoDB
            // caseRepository.save(mongoCase);

            log.info("案件数据同步完成: {} (MongoDB操作已暂时禁用)", caseId);
        } catch (Exception e) {
            log.error("同步案件数据到MongoDB失败: {} (MongoDB操作已暂时禁用)", caseId, e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "同步案件数据到MongoDB失败: " + e.getMessage());
        }
    }

    /**
     * 转换为MongoDB案件实体
     */
//    private Case convertToMongoCase(CaseInfoEntity caseInfoEntity,
//            List<CasePersonalEntity> personalEntities,
//            List<CaseCompanyEntity> companyEntities,
//            List<CaseAttachmentEntity> attachmentEntities,
//            List<CaseVehicleEntity> vehicleEntities,
//            List<CasePunishEntity> punishEntities,
//            List<CaseOfficerEntity> officerEntities) {
//        Case mongoCase = new Case();
//
//        // 复制主表数据
//        BeanUtils.copyProperties(caseInfoEntity, mongoCase);
//        mongoCase.setCaseId(caseInfoEntity.getCaseId());
//        mongoCase.setSourceTable("LE_CaseInfo");
//        mongoCase.setLastSyncTime(LocalDateTime.now());
//
//        // 转换当事人数据
//        if (personalEntities != null && !personalEntities.isEmpty()) {
//            List<CaseParty> parties = personalEntities.stream()
//                    .map(this::convertToMongoParty)
//                    .collect(Collectors.toList());
//            mongoCase.setParties(parties);
//        }
//
//        // 转换附件数据
//        if (attachmentEntities != null && !attachmentEntities.isEmpty()) {
//            List<CaseDocument> documents = attachmentEntities.stream()
//                    .map(this::convertToMongoDocument)
//                    .collect(Collectors.toList());
//            mongoCase.setDocuments(documents);
//        }
//
//        return mongoCase;
//    }

    /**
     * 转换为MongoDB当事人实体
     */
    private CaseParty convertToMongoParty(CasePersonalEntity personalEntity) {
        CaseParty party = new CaseParty();
        BeanUtils.copyProperties(personalEntity, party);
        return party;
    }

    /**
     * 转换为MongoDB文件实体
     */
    private CaseDocument convertToMongoDocument(CaseAttachmentEntity attachmentEntity) {
        CaseDocument document = new CaseDocument();
        BeanUtils.copyProperties(attachmentEntity, document);
        return document;
    }
}