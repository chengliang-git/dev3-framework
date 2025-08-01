package com.guanwei.tles.casetransfer.entity.oracle;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanwei.framework.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Oracle案件当事单位信息实体类 - 对应CaseCompany.cs
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("CASE_COMPANY")
public class CaseCompanyEntity extends BaseEntity {

    /**
     * 案件Id
     */
    private String caseId;

    /**
     * 业户Id
     */
    private String ownerId;

    /**
     * 企业名称
     */
    private String companyName;

    /**
     * 统一社会信用代码
     */
    private String socialCreditCode;

    /**
     * 法人代表
     */
    private String legalName;

    /**
     * 法人身份证号
     */
    private String legalIdCertNum;

    /**
     * 单位地址
     */
    private String address;

    /**
     * 注册地址行政区划名称
     */
    private String registAreaName;

    /**
     * 注册地址行政区划
     */
    private String registAreaCode;

    /**
     * 经营地址行政区划
     */
    private String operateAreaCode;

    /**
     * 经营地址
     */
    private String operateAddress;

    /**
     * 单位联系电话
     */
    private String phone;

    /**
     * 经营许可证号
     */
    private String ownerLicenseNum;

    /**
     * 许可证初领日期
     */
    private LocalDateTime licenseFirstDate;

    /**
     * 许可证发证日期
     */
    private LocalDateTime licenseIssueDate;

    /**
     * 发证机关
     */
    private String issueOrg;

    /**
     * 法人职务
     */
    private String legalPost;

    /**
     * 法人身份证类型
     */
    private Integer legalIdCertType;

    /**
     * 工商注册登记号
     */
    private String registCode;
} 