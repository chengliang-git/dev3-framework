package com.guanwei.tles.casetransfer.entity.oracle;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanwei.framework.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Oracle案件执法人员实体类 - 对应CaseOfficer.cs
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("CASE_OFFICER")
public class CaseOfficerEntity extends BaseEntity {

    /**
     * 案件执法人员Id
     */
    private String caseOfficerId;

    /**
     * 执法人员Id
     */
    private String officerId;

    /**
     * 执法人员姓名
     */
    private String officerName;

    /**
     * 执法证号
     */
    private String lawCardNum;

    /**
     * 执法单元Id
     */
    private String unitId;

    /**
     * 案件Id
     */
    private String caseId;

    /**
     * 所属机构
     */
    private String orgName;

    /**
     * 所在部门
     */
    private String depName;

    /**
     * 人员类型
     */
    private Integer officerType;

    /**
     * 签名图片URL
     */
    private String signPicUrl;

    /**
     * 执法证图片URL
     */
    private String officerCredentialPicUrl;
} 