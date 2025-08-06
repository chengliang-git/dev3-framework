package com.guanwei.tles.casetransfer.entity;

import lombok.Data;
// import org.springframework.data.mongodb.core.mapping.Field; // 暂时禁用MongoDB

import java.time.LocalDateTime;

/**
 * 案件当事人实体类 - MongoDB功能暂时禁用
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Data
public class CaseParty {

    /**
     * 当事人ID
     */
    // @Field("partyId") // 暂时禁用MongoDB
    private String partyId;

    /**
     * 当事人姓名
     */
    // @Field("name") // 暂时禁用MongoDB
    private String name;

    /**
     * 证件类型
     */
    // @Field("idType") // 暂时禁用MongoDB
    private String idType;

    /**
     * 证件号码
     */
    // @Field("idNumber") // 暂时禁用MongoDB
    private String idNumber;

    /**
     * 联系电话
     */
    // @Field("phone") // 暂时禁用MongoDB
    private String phone;

    /**
     * 地址
     */
    // @Field("address") // 暂时禁用MongoDB
    private String address;

    /**
     * 当事人类型（个人/单位）
     */
    // @Field("partyType") // 暂时禁用MongoDB
    private String partyType;

    /**
     * 创建时间
     */
    // @Field("createTime") // 暂时禁用MongoDB
    private LocalDateTime createTime;
} 