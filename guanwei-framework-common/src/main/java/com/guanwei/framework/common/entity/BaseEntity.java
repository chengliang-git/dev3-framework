package com.guanwei.framework.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 支持32位GUID主键和审计字段
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Schema(description = "基础实体")
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID (32位GUID)")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "创建时间")
    @TableField(value = "createTime", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField(value = "modifyTime", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifyTime;

    @Schema(description = "创建人")
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private String creator;

    @Schema(description = "修改人")
    @TableField(value = "modifier", fill = FieldFill.INSERT_UPDATE)
    private String modifier;

    @Schema(description = "删除标记（0：未删除，1：已删除）")
    @TableLogic
    @TableField(value = "delFlag")
    private Integer delFlag;

    @Schema(description = "顺序号（由前端提供）")
    @TableField(value = "orderNo")
    private Integer orderNo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(LocalDateTime modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }
}