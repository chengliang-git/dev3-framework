package com.guanwei.tles.casetransfer.entity.oracle;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanwei.framework.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Oracle案件信息实体类 - 对应CaseInfo.cs
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("LE_CaseInfo")
public class CaseInfoEntity extends BaseEntity {

    /**
     * 案件Id
     */
    private String caseId;

    /**
     * 车船名称
     */
    private String vehicleShipName;

    /**
     * 违法对象
     */
    private Integer illegalObjects;

    /**
     * 当事单位名称
     */
    private String companyName;

    /**
     * 当事个人姓名
     */
    private String partyName;

    /**
     * 当事个人证件号码
     */
    private String idCardNum;

    /**
     * 案件状态
     */
    private Integer state;

    /**
     * 立案时间(检查时间)
     */
    private LocalDateTime caseFilingTime;

    /**
     * 违法地点
     */
    private String illegalLocation;

    /**
     * 案件登记号
     */
    private String caseNo;

    /**
     * 案件来源
     */
    private Integer source;

    /**
     * 违法行为
     */
    private String illegalBehavior;

    /**
     * 案由
     */
    private String caseReason;

    /**
     * 违法内容
     */
    private String illegalContent;

    /**
     * 立案依据
     */
    private String caseBasis;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 办理程序
     */
    private Integer caseType;

    /**
     * 违法时间
     */
    private LocalDateTime illegalTime;

    /**
     * 执法人员
     */
    private String officers;

    /**
     * 违法行为Id
     */
    private String illegalId;

    /**
     * 地区编码
     */
    private String areaCode;

    /**
     * 所属机构Id
     */
    private String orgId;

    /**
     * 办理机构Id
     */
    private String handleOrgId;

    /**
     * 结案时间
     */
    private LocalDateTime closeTime;

    /**
     * 处罚状态
     */
    private Integer punishState;

    /**
     * 受案时间
     */
    private LocalDateTime receiptDate;

    /**
     * 是否提交给内勤
     */
    private Integer isCommit;

    /**
     * 案件分类
     */
    private Integer caseCategory;

    /**
     * 是否配合检查
     */
    private Integer acceptCheck;

    /**
     * 冲关闯关
     */
    private String rushEmigrate;

    /**
     * 检查地点
     */
    private String checkLocation;

    /**
     * 车辆情况
     */
    private Integer vehicleSitu;

    /**
     * 违法地点经度
     */
    private BigDecimal longitude;

    /**
     * 违法地点纬度
     */
    private BigDecimal latitude;

    /**
     * 执法类别
     */
    private Integer lawEnforCategory;

    /**
     * 案件行为行业类别
     */
    private Integer industryType;

    /**
     * 登记人部门Id
     */
    private String depId;

    /**
     * 执法部门名称
     */
    private String depName;

    /**
     * 案件登记地区编码
     */
    private String registAreaCode;

    /**
     * 内勤执法单元Id
     */
    private String innerUnitId;

    /**
     * 统一社会信用代码
     */
    private String socialCreditCode;

    /**
     * 办理状态
     */
    private Integer processStatus;

    /**
     * 案件来源信息
     */
    private String sourceInfo;

    /**
     * 案件号模板
     */
    private String caseNoTmpl;

    /**
     * 执法机构名称
     */
    private String orgName;

    /**
     * 最终处理决定
     */
    private String finalDecision;

    /**
     * 是否抄告案件
     */
    private Integer isDeliverCase;

    /**
     * 处罚决定日期
     */
    private LocalDateTime penaltyDecisionTime;

    /**
     * 是否不予行政处罚
     */
    private Integer notPunish;

    /**
     * 结案人员
     */
    private String closer;

    /**
     * 案件发生地区划代码
     */
    private String regionCode;
} 