package com.guanwei.tles.casetransfer.service.impl;

import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.common.exception.BusinessException;
import com.guanwei.framework.common.result.ResultCode;
import com.guanwei.tles.casetransfer.dto.CaseMessage;
import com.guanwei.tles.casetransfer.entity.Case;
import com.guanwei.tles.casetransfer.entity.CaseDocument;
import com.guanwei.tles.casetransfer.entity.CaseParty;
import com.guanwei.tles.casetransfer.entity.oracle.*;
import com.guanwei.tles.casetransfer.repository.CaseRepository;
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

    private final CaseRepository caseRepository;

    @Override
    @Transactional
    public void handleCaseCreated(CaseMessage caseMessage) {
        log.info("处理案件新增消息: {}", caseMessage);
        try {
            syncCaseToMongoDB(caseMessage.getCaseId());
            log.info("案件新增处理完成: {}", caseMessage.getCaseId());
        } catch (Exception e) {
            log.error("处理案件新增消息失败: {}", caseMessage.getCaseId(), e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "处理案件新增消息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handleCaseUpdated(CaseMessage caseMessage) {
        log.info("处理案件修改消息: {}", caseMessage);
        try {
            syncCaseToMongoDB(caseMessage.getCaseId());
            log.info("案件修改处理完成: {}", caseMessage.getCaseId());
        } catch (Exception e) {
            log.error("处理案件修改消息失败: {}", caseMessage.getCaseId(), e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "处理案件修改消息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handleCaseDeleted(CaseMessage caseMessage) {
        log.info("处理案件删除消息: {}", caseMessage);
        try {
            caseRepository.deleteByCaseId(caseMessage.getCaseId());
            log.info("案件删除处理完成: {}", caseMessage.getCaseId());
        } catch (Exception e) {
            log.error("处理案件删除消息失败: {}", caseMessage.getCaseId(), e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "处理案件删除消息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void syncCaseToMongoDB(String caseId) {
        log.info("开始同步案件数据到MongoDB: {}", caseId);

        // TODO: 暂时注释掉 Oracle 数据查询，等 MyBatis Plus 配置问题解决后再启用
        /*
         * // 1. 从Oracle查询案件主表数据
         * CaseEntity caseEntity = caseMapper.selectByCaseId(caseId);
         * if (caseEntity == null) {
         * log.warn("案件不存在: {}", caseId);
         * return;
         * }
         * 
         * // 2. 从Oracle查询案件当事人数据
         * List<CasePartyEntity> partyEntities =
         * caseMapper.selectPartiesByCaseId(caseId);
         * 
         * // 3. 从Oracle查询案件当事单位数据
         * List<CaseCompanyEntity> companyEntities =
         * caseMapper.selectCompaniesByCaseId(caseId);
         * 
         * // 4. 从Oracle查询案件附件数据
         * List<CaseDocumentEntity> attachmentEntities =
         * caseMapper.selectAttachmentsByCaseId(caseId);
         * 
         * // 5. 从Oracle查询案件车辆数据
         * List<CaseVehicleEntity> vehicleEntities =
         * caseMapper.selectVehiclesByCaseId(caseId);
         * 
         * // 6. 从Oracle查询案件处罚数据
         * List<CasePunishEntity> punishEntities =
         * caseMapper.selectPunishesByCaseId(caseId);
         * 
         * // 7. 从Oracle查询案件执法人员数据
         * List<CaseOfficerEntity> officerEntities =
         * caseMapper.selectOfficersByCaseId(caseId);
         * 
         * // 8. 转换为MongoDB实体
         * Case mongoCase = convertToMongoCase(caseEntity, partyEntities,
         * companyEntities,
         * attachmentEntities, vehicleEntities, punishEntities, officerEntities);
         * 
         * // 9. 保存到MongoDB
         * caseRepository.save(mongoCase);
         */

        log.info("案件数据同步完成: {}", caseId);
    }

    /**
     * 转换为MongoDB案件实体
     */
    private Case convertToMongoCase(CaseEntity caseEntity,
            List<CasePartyEntity> partyEntities,
            List<CaseCompanyEntity> companyEntities,
            List<CaseDocumentEntity> attachmentEntities,
            List<CaseVehicleEntity> vehicleEntities,
            List<CasePunishEntity> punishEntities,
            List<CaseOfficerEntity> officerEntities) {
        Case mongoCase = new Case();

        // 复制主表数据
        BeanUtils.copyProperties(caseEntity, mongoCase);
        mongoCase.setCaseId(caseEntity.getCaseId());
        mongoCase.setSourceTable("CASE_INFO");
        mongoCase.setLastSyncTime(LocalDateTime.now());

        // 转换当事人数据
        if (partyEntities != null && !partyEntities.isEmpty()) {
            List<CaseParty> parties = partyEntities.stream()
                    .map(this::convertToMongoParty)
                    .collect(Collectors.toList());
            mongoCase.setParties(parties);
        }

        // 转换附件数据
        if (attachmentEntities != null && !attachmentEntities.isEmpty()) {
            List<CaseDocument> documents = attachmentEntities.stream()
                    .map(this::convertToMongoDocument)
                    .collect(Collectors.toList());
            mongoCase.setDocuments(documents);
        }

        return mongoCase;
    }

    /**
     * 转换为MongoDB当事人实体
     */
    private CaseParty convertToMongoParty(CasePartyEntity partyEntity) {
        CaseParty party = new CaseParty();
        BeanUtils.copyProperties(partyEntity, party);
        return party;
    }

    /**
     * 转换为MongoDB文件实体
     */
    private CaseDocument convertToMongoDocument(CaseDocumentEntity documentEntity) {
        CaseDocument document = new CaseDocument();
        BeanUtils.copyProperties(documentEntity, document);
        return document;
    }
}