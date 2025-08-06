//package com.guanwei.tles.casetransfer.entity;
//
////import com.guanwei.framework.common.entity.BaseMongoEntity;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//// import org.springframework.data.mongodb.core.mapping.Document; // 暂时禁用MongoDB
//// import org.springframework.data.mongodb.core.mapping.Field; // 暂时禁用MongoDB
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//
///**
// * 案件实体类 - MongoDB功能暂时禁用
// *
// * @author Guanwei Framework
// * @since 1.0.0
// */
//@Data
//@EqualsAndHashCode(callSuper = true)
//// @Document(collection = "cases") // 暂时禁用MongoDB
//public class Case extends BaseMongoEntity {
//
//    /**
//     * 案件ID（来自Oracle源表）
//     */
//    // @Field("caseId") // 暂时禁用MongoDB
//    private String caseId;
//
//    /**
//     * 车船名称
//     */
//    // @Field("vehicleShipName") // 暂时禁用MongoDB
//    private String vehicleShipName;
//
//    /**
//     * 违法对象
//     */
//    // @Field("illegalObjects") // 暂时禁用MongoDB
//    private Integer illegalObjects;
//
//    /**
//     * 当事单位名称
//     */
//    // @Field("companyName") // 暂时禁用MongoDB
//    private String companyName;
//
//    /**
//     * 当事个人姓名
//     */
//    // @Field("partyName") // 暂时禁用MongoDB
//    private String partyName;
//
//    /**
//     * 当事个人证件号码
//     */
//    // @Field("idCardNum") // 暂时禁用MongoDB
//    private String idCardNum;
//
//    /**
//     * 案件状态
//     */
//    // @Field("state") // 暂时禁用MongoDB
//    private Integer state;
//
//    /**
//     * 立案时间(检查时间)
//     */
//    // @Field("caseFilingTime") // 暂时禁用MongoDB
//    private LocalDateTime caseFilingTime;
//
//    /**
//     * 违法地点
//     */
//    // @Field("illegalLocation") // 暂时禁用MongoDB
//    private String illegalLocation;
//
//    /**
//     * 案件登记号
//     */
//    // @Field("caseNo") // 暂时禁用MongoDB
//    private String caseNo;
//
//    /**
//     * 案件来源
//     */
//    // @Field("source") // 暂时禁用MongoDB
//    private Integer source;
//
//    /**
//     * 违法行为
//     */
//    // @Field("illegalBehavior") // 暂时禁用MongoDB
//    private String illegalBehavior;
//
//    /**
//     * 案由
//     */
//    // @Field("caseReason") // 暂时禁用MongoDB
//    private String caseReason;
//
//    /**
//     * 违法内容
//     */
//    // @Field("illegalContent") // 暂时禁用MongoDB
//    private String illegalContent;
//
//    /**
//     * 立案依据
//     */
//    // @Field("caseBasis") // 暂时禁用MongoDB
//    private String caseBasis;
//
//    /**
//     * 办理程序
//     */
//    // @Field("caseType") // 暂时禁用MongoDB
//    private Integer caseType;
//
//    /**
//     * 违法时间
//     */
//    // @Field("illegalTime") // 暂时禁用MongoDB
//    private LocalDateTime illegalTime;
//
//    /**
//     * 执法人员
//     */
//    // @Field("officers") // 暂时禁用MongoDB
//    private String officers;
//
//    /**
//     * 违法行为Id
//     */
//    // @Field("illegalId") // 暂时禁用MongoDB
//    private String illegalId;
//
//    /**
//     * 地区编码
//     */
//    // @Field("areaCode") // 暂时禁用MongoDB
//    private String areaCode;
//
//    /**
//     * 所属机构Id
//     */
//    // @Field("orgId") // 暂时禁用MongoDB
//    private String orgId;
//
//    /**
//     * 办理机构Id
//     */
//    // @Field("handleOrgId") // 暂时禁用MongoDB
//    private String handleOrgId;
//
//    /**
//     * 结案时间
//     */
//    // @Field("closeTime") // 暂时禁用MongoDB
//    private LocalDateTime closeTime;
//
//    /**
//     * 处罚状态
//     */
//    // @Field("punishState") // 暂时禁用MongoDB
//    private Integer punishState;
//
//    /**
//     * 受案时间
//     */
//    // @Field("receiptDate") // 暂时禁用MongoDB
//    private LocalDateTime receiptDate;
//
//    /**
//     * 是否提交给内勤
//     */
//    // @Field("isCommit") // 暂时禁用MongoDB
//    private Integer isCommit;
//
//    /**
//     * 案件分类
//     */
//    // @Field("caseCategory") // 暂时禁用MongoDB
//    private Integer caseCategory;
//
//    /**
//     * 是否配合检查
//     */
//    // @Field("acceptCheck") // 暂时禁用MongoDB
//    private Integer acceptCheck;
//
//    /**
//     * 冲关闯关
//     */
//    // @Field("rushEmigrate") // 暂时禁用MongoDB
//    private String rushEmigrate;
//
//    /**
//     * 检查地点
//     */
//    // @Field("checkLocation") // 暂时禁用MongoDB
//    private String checkLocation;
//
//    /**
//     * 车辆情况
//     */
//    // @Field("vehicleSitu") // 暂时禁用MongoDB
//    private Integer vehicleSitu;
//
//    /**
//     * 违法地点经度
//     */
//    // @Field("longitude") // 暂时禁用MongoDB
//    private BigDecimal longitude;
//
//    /**
//     * 违法地点纬度
//     */
//    // @Field("latitude") // 暂时禁用MongoDB
//    private BigDecimal latitude;
//
//    /**
//     * 执法类别
//     */
//    // @Field("lawEnforCategory") // 暂时禁用MongoDB
//    private Integer lawEnforCategory;
//
//    /**
//     * 案件行为行业类别
//     */
//    // @Field("industryType") // 暂时禁用MongoDB
//    private Integer industryType;
//
//    /**
//     * 登记人部门Id
//     */
//    // @Field("depId") // 暂时禁用MongoDB
//    private String depId;
//
//    /**
//     * 执法部门名称
//     */
//    // @Field("depName") // 暂时禁用MongoDB
//    private String depName;
//
//    /**
//     * 案件登记地区编码
//     */
//    // @Field("registAreaCode") // 暂时禁用MongoDB
//    private String registAreaCode;
//
//    /**
//     * 内勤执法单元Id
//     */
//    // @Field("innerUnitId") // 暂时禁用MongoDB
//    private String innerUnitId;
//
//    /**
//     * 统一社会信用代码
//     */
//    // @Field("socialCreditCode") // 暂时禁用MongoDB
//    private String socialCreditCode;
//
//    /**
//     * 办理状态
//     */
//    // @Field("processStatus") // 暂时禁用MongoDB
//    private Integer processStatus;
//
//    /**
//     * 案件来源信息
//     */
//    // @Field("sourceInfo") // 暂时禁用MongoDB
//    private String sourceInfo;
//
//    /**
//     * 案件号模板
//     */
//    // @Field("caseNoTmpl") // 暂时禁用MongoDB
//    private String caseNoTmpl;
//
//    /**
//     * 执法机构名称
//     */
//    // @Field("orgName") // 暂时禁用MongoDB
//    private String orgName;
//
//    /**
//     * 最终处理决定
//     */
//    // @Field("finalDecision") // 暂时禁用MongoDB
//    private String finalDecision;
//
//    /**
//     * 是否抄告案件
//     */
//    // @Field("isDeliverCase") // 暂时禁用MongoDB
//    private Integer isDeliverCase;
//
//    /**
//     * 处罚决定日期
//     */
//    // @Field("penaltyDecisionTime") // 暂时禁用MongoDB
//    private LocalDateTime penaltyDecisionTime;
//
//    /**
//     * 是否不予行政处罚
//     */
//    // @Field("notPunish") // 暂时禁用MongoDB
//    private Integer notPunish;
//
//    /**
//     * 结案人员
//     */
//    // @Field("closer") // 暂时禁用MongoDB
//    private String closer;
//
//    /**
//     * 案件发生地区划代码
//     */
//    // @Field("regionCode") // 暂时禁用MongoDB
//    private String regionCode;
//
//    /**
//     * 实际处罚金额
//     */
//    // @Field("actualPunish") // 暂时禁用MongoDB
//    private BigDecimal actualPunish;
//
//    /**
//     * 当事人信息
//     */
//    // @Field("parties") // 暂时禁用MongoDB
//    private List<CaseParty> parties;
//
//    /**
//     * 案件文件
//     */
//    // @Field("documents") // 暂时禁用MongoDB
//    private List<CaseDocument> documents;
//
//    /**
//     * 案件备注
//     */
//    // @Field("remarks") // 暂时禁用MongoDB
//    private String remarks;
//
//    /**
//     * 数据来源（Oracle表名）
//     */
//    // @Field("sourceTable") // 暂时禁用MongoDB
//    private String sourceTable;
//
//    /**
//     * 最后同步时间
//     */
//    // @Field("lastSyncTime") // 暂时禁用MongoDB
//    private LocalDateTime lastSyncTime;
//
//    /**
//     * 案件编号
//     */
//    // @Field("caseNumber") // 暂时禁用MongoDB
//    private String caseNumber;
//}