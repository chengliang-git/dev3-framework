package com.guanwei.tles.casetransfer.entity.oracle;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanwei.framework.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Oracle案件当事人个人实体类 - 对应CasePersonal.cs
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("CASE_PERSONAL")
public class CasePartyEntity extends BaseEntity {

    /**
     * 案件Id
     */
    private String caseId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 地区编码
     */
    private String areaCode;

    /**
     * 地区编码名称
     */
    private String areaCodeName;

    /**
     * 住址
     */
    private String address;

    /**
     * 工作职务
     */
    private String post;

    /**
     * 证件类型
     */
    private Integer idCardType;

    /**
     * 证件号码
     */
    private String idCardNum;

    /**
     * 从业资格证号
     */
    private String pracCertNum;

    /**
     * 从业资格证发证机关
     */
    private String issueOrg;

    /**
     * 所在单位
     */
    private String companyName;

    /**
     * 邮编
     */
    private String postcode;

    /**
     * 民族
     */
    private String nation;

    /**
     * 统一社会信用代码
     */
    private String socialCreditCode;

    /**
     * 经营许可证号
     */
    private String ownerLicenseNum;

    /**
     * 业户Id
     */
    private String ownerId;

    /**
     * 单位及职务
     */
    private String companyPost;

    /**
     * 是否个体户
     */
    private Integer isSelfEmployed;

    /**
     * 字号名称
     */
    private String ownerName;

    /**
     * 工商注册登记号
     */
    private String registCode;
} 