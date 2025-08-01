package com.guanwei.tles.casetransfer.entity.oracle;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanwei.framework.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Oracle案件车辆实体类 - 对应CaseVehicle.cs
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("CASE_VEHICLE")
public class CaseVehicleEntity extends BaseEntity {

    /**
     * 案件Id
     */
    private String caseId;

    /**
     * 车辆Id
     */
    private String vehicleId;

    /**
     * 车牌号码
     */
    private String vehicleNum;

    /**
     * 道路运输证字号
     */
    private String tranLicenseNum;

    /**
     * 车牌颜色
     */
    private String plateColor;

    /**
     * 挂车牌号
     */
    private String trailerNum;

    /**
     * 是否临牌
     */
    private Integer isLin;

    /**
     * 车辆规格
     */
    private String specification;

    /**
     * 车辆结构
     */
    private String structure;

    /**
     * 车辆类型代码
     */
    private String vehicleType;

    /**
     * 厂牌型号
     */
    private String brandModel;

    /**
     * 车籍地
     */
    private String vehicleArea;

    /**
     * 所有人业户
     */
    private String ownerName;

    /**
     * 所有人业户地址
     */
    private String ownerAddress;

    /**
     * 所有人经营许可证号
     */
    private String ownerLicenseNum;

    /**
     * 所有人联系电话
     */
    private String ownerPhone;

    /**
     * 驾驶员姓名
     */
    private String driverName;

    /**
     * 驾驶员住址
     */
    private String driverAddress;

    /**
     * 驾驶员证件类型
     */
    private Integer driverIdCardType;

    /**
     * 驾驶员证件号码
     */
    private String driverIdCardNum;

    /**
     * 驾驶员从业资格证号
     */
    private String driverPracCertNum;

    /**
     * 资格证省市县名
     */
    private String pracCertAddress;

    /**
     * 资格证地区代码
     */
    private String pracCertAreaCode;

    /**
     * 从业资格证发证机关
     */
    private String driverIssueOrg;

    /**
     * 驾驶员与车辆所有人关系
     */
    private String driverOwnerRelation;

    /**
     * 联系电话
     */
    private String driverPhone;

    /**
     * 业户Id
     */
    private String ownerId;

    /**
     * 行业类别
     */
    private Integer transType;

    /**
     * 驾驶员与案件关系
     */
    private String caseRelation;

    /**
     * 单位及职务
     */
    private String companyPosition;

    /**
     * 车轴数
     */
    private Integer axleNum;
} 