package com.guanwei.tles.casetransfer.entity;

import com.guanwei.framework.common.entity.BaseMongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 案件实体类
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "cases")
public class Case extends BaseMongoEntity {

    /**
     * 案件ID（来自Oracle源表）
     */
    @Field("caseId")
    private String caseId;

    /**
     * 车船名称
     */
    @Field("vehicleShipName")
    private String vehicleShipName;

    /**
     * 违法对象
     */
    @Field("illegalObjects")
    private Integer illegalObjects;

    /**
     * 当事单位名称
     */
    @Field("companyName")
    private String companyName;

    /**
     * 当事个人姓名
     */
    @Field("partyName")
    private String partyName;

    /**
     * 当事个人证件号码
     */
    @Field("idCardNum")
    private String idCardNum;

    /**
     * 案件状态
     */
    @Field("state")
    private Integer state;

    /**
     * 立案时间(检查时间)
     */
    @Field("caseFilingTime")
    private LocalDateTime caseFilingTime;

    /**
     * 违法地点
     */
    @Field("illegalLocation")
    private String illegalLocation;

    /**
     * 案件登记号
     */
    @Field("caseNo")
    private String caseNo;

    /**
     * 案件来源
     */
    @Field("source")
    private Integer source;

    /**
     * 违法行为
     */
    @Field("illegalBehavior")
    private String illegalBehavior;

    /**
     * 案由
     */
    @Field("caseReason")
    private String caseReason;

    /**
     * 违法内容
     */
    @Field("illegalContent")
    private String illegalContent;

    /**
     * 立案依据
     */
    @Field("caseBasis")
    private String caseBasis;

    /**
     * 办理程序
     */
    @Field("caseType")
    private Integer caseType;

    /**
     * 违法时间
     */
    @Field("illegalTime")
    private LocalDateTime illegalTime;

    /**
     * 执法人员
     */
    @Field("officers")
    private String officers;

    /**
     * 违法行为Id
     */
    @Field("illegalId")
    private String illegalId;

    /**
     * 地区编码
     */
    @Field("areaCode")
    private String areaCode;

    /**
     * 所属机构Id
     */
    @Field("orgId")
    private String orgId;

    /**
     * 办理机构Id
     */
    @Field("handleOrgId")
    private String handleOrgId;

    /**
     * 结案时间
     */
    @Field("closeTime")
    private LocalDateTime closeTime;

    /**
     * 处罚状态
     */
    @Field("punishState")
    private Integer punishState;

    /**
     * 受案时间
     */
    @Field("receiptDate")
    private LocalDateTime receiptDate;

    /**
     * 是否提交给内勤
     */
    @Field("isCommit")
    private Integer isCommit;

    /**
     * 案件分类
     */
    @Field("caseCategory")
    private Integer caseCategory;

    /**
     * 是否配合检查
     */
    @Field("acceptCheck")
    private Integer acceptCheck;

    /**
     * 冲关闯关
     */
    @Field("rushEmigrate")
    private String rushEmigrate;

    /**
     * 检查地点
     */
    @Field("checkLocation")
    private String checkLocation;

    /**
     * 车辆情况
     */
    @Field("vehicleSitu")
    private Integer vehicleSitu;

    /**
     * 违法地点经度
     */
    @Field("longitude")
    private BigDecimal longitude;

    /**
     * 违法地点纬度
     */
    @Field("latitude")
    private BigDecimal latitude;

    /**
     * 执法类别
     */
    @Field("lawEnforCategory")
    private Integer lawEnforCategory;

    /**
     * 案件行为行业类别
     */
    @Field("industryType")
    private Integer industryType;

    /**
     * 登记人部门Id
     */
    @Field("depId")
    private String depId;

    /**
     * 执法部门名称
     */
    @Field("depName")
    private String depName;

    /**
     * 案件登记地区编码
     */
    @Field("registAreaCode")
    private String registAreaCode;

    /**
     * 内勤执法单元Id
     */
    @Field("innerUnitId")
    private String innerUnitId;

    /**
     * 统一社会信用代码
     */
    @Field("socialCreditCode")
    private String socialCreditCode;

    /**
     * 办理状态
     */
    @Field("processStatus")
    private Integer processStatus;

    /**
     * 案件来源信息
     */
    @Field("sourceInfo")
    private String sourceInfo;

    /**
     * 案件号模板
     */
    @Field("caseNoTmpl")
    private String caseNoTmpl;

    /**
     * 执法机构名称
     */
    @Field("orgName")
    private String orgName;

    /**
     * 最终处理决定
     */
    @Field("finalDecision")
    private String finalDecision;

    /**
     * 是否抄告案件
     */
    @Field("isDeliverCase")
    private Integer isDeliverCase;

    /**
     * 处罚决定日期
     */
    @Field("penaltyDecisionTime")
    private LocalDateTime penaltyDecisionTime;

    /**
     * 是否不予行政处罚
     */
    @Field("notPunish")
    private Integer notPunish;

    /**
     * 结案人员
     */
    @Field("closer")
    private String closer;

    /**
     * 案件发生地区划代码
     */
    @Field("regionCode")
    private String regionCode;

    /**
     * 实际处罚金额
     */
    @Field("actualPunish")
    private BigDecimal actualPunish;

    /**
     * 当事人信息
     */
    @Field("parties")
    private List<CaseParty> parties;

    /**
     * 案件文件
     */
    @Field("documents")
    private List<CaseDocument> documents;

    /**
     * 案件备注
     */
    @Field("remarks")
    private String remarks;

    /**
     * 数据来源（Oracle表名）
     */
    @Field("sourceTable")
    private String sourceTable;

    /**
     * 最后同步时间
     */
    @Field("lastSyncTime")
    private LocalDateTime lastSyncTime;

    /**
     * 案件编号
     */
    @Field("caseNumber")
    private String caseNumber;
}