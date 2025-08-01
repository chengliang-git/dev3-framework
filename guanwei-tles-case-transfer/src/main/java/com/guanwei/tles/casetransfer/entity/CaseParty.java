package com.guanwei.tles.casetransfer.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 案件当事人实体类
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Data
public class CaseParty {

    /**
     * 人员姓名
     */
    @Field("personName")
    private String personName;

    /**
     * 性别
     */
    @Field("sex")
    private Integer sex;

    /**
     * 年龄
     */
    @Field("age")
    private Integer age;

    /**
     * 联系电话
     */
    @Field("phone")
    private String phone;

    /**
     * 地区编码
     */
    @Field("areaCode")
    private String areaCode;

    /**
     * 地区编码名称
     */
    @Field("areaCodeName")
    private String areaCodeName;

    /**
     * 住址
     */
    @Field("address")
    private String address;

    /**
     * 工作职务
     */
    @Field("post")
    private String post;

    /**
     * 证件类型
     */
    @Field("idCardType")
    private Integer idCardType;

    /**
     * 证件号码
     */
    @Field("idCardNum")
    private String idCardNum;

    /**
     * 从业资格证号
     */
    @Field("pracCertNum")
    private String pracCertNum;

    /**
     * 从业资格证发证机关
     */
    @Field("issueOrg")
    private String issueOrg;

    /**
     * 所在单位
     */
    @Field("companyName")
    private String companyName;

    /**
     * 邮编
     */
    @Field("postcode")
    private String postcode;

    /**
     * 民族
     */
    @Field("nation")
    private String nation;

    /**
     * 统一社会信用代码
     */
    @Field("socialCreditCode")
    private String socialCreditCode;

    /**
     * 经营许可证号
     */
    @Field("ownerLicenseNum")
    private String ownerLicenseNum;

    /**
     * 业户Id
     */
    @Field("ownerId")
    private String ownerId;

    /**
     * 单位及职务
     */
    @Field("companyPost")
    private String companyPost;

    /**
     * 是否个体户
     */
    @Field("isSelfEmployed")
    private Integer isSelfEmployed;

    /**
     * 字号名称
     */
    @Field("ownerName")
    private String ownerName;

    /**
     * 工商注册登记号
     */
    @Field("registCode")
    private String registCode;
} 