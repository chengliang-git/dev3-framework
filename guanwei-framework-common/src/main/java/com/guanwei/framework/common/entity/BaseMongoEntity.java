//package com.guanwei.framework.common.entity;
//
//import com.fasterxml.jackson.annotation.JsonFormat;
//import io.swagger.v3.oas.annotations.media.Schema;
//import org.springframework.data.annotation.CreatedBy;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.annotation.LastModifiedBy;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.mongodb.core.mapping.Field;
//
//import java.io.Serializable;
//import java.time.LocalDateTime;
//
///**
// * MongoDB基础实体类
// * 支持MongoDB的审计字段和32位GUID主键
// *
// * @author Enterprise Framework
// * @since 1.0.0
// */
//@Schema(description = "MongoDB基础实体")
//public abstract class BaseMongoEntity implements Serializable {
//
//    private static final long serialVersionUID = 1L;
//
//    @Schema(description = "主键ID (32位GUID)")
//    @Id
//    private String id;
//
//    @Schema(description = "创建时间")
//    @CreatedDate
//    @Field("createTime")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime createTime;
//
//    @Schema(description = "修改时间")
//    @LastModifiedDate
//    @Field("modifyTime")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime modifyTime;
//
//    @Schema(description = "创建人")
//    @CreatedBy
//    @Field("creator")
//    private String creator;
//
//    @Schema(description = "修改人")
//    @LastModifiedBy
//    @Field("modifier")
//    private String modifier;
//
//    @Schema(description = "删除标记（0：未删除，1：已删除）")
//    @Field("delFlag")
//    private Integer delFlag = 0;
//
//    @Schema(description = "顺序号（由前端提供）")
//    @Field("orderNo")
//    private Integer orderNo;
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public LocalDateTime getCreateTime() {
//        return createTime;
//    }
//
//    public void setCreateTime(LocalDateTime createTime) {
//        this.createTime = createTime;
//    }
//
//    public LocalDateTime getModifyTime() {
//        return modifyTime;
//    }
//
//    public void setModifyTime(LocalDateTime modifyTime) {
//        this.modifyTime = modifyTime;
//    }
//
//    public String getCreator() {
//        return creator;
//    }
//
//    public void setCreator(String creator) {
//        this.creator = creator;
//    }
//
//    public String getModifier() {
//        return modifier;
//    }
//
//    public void setModifier(String modifier) {
//        this.modifier = modifier;
//    }
//
//    public Integer getDelFlag() {
//        return delFlag;
//    }
//
//    public void setDelFlag(Integer delFlag) {
//        this.delFlag = delFlag;
//    }
//
//    public Integer getOrderNo() {
//        return orderNo;
//    }
//
//    public void setOrderNo(Integer orderNo) {
//        this.orderNo = orderNo;
//    }
//}