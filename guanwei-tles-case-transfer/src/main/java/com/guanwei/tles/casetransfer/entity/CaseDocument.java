package com.guanwei.tles.casetransfer.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 案件文件实体类
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Data
public class CaseDocument {

    /**
     * 附件Id
     */
    @Field("attachId")
    private String attachId;

    /**
     * 附件名称
     */
    @Field("attachName")
    private String attachName;

    /**
     * 相对路径
     */
    @Field("filePath")
    private String filePath;

    /**
     * 文件大小(KB)
     */
    @Field("fileSize")
    private BigDecimal fileSize;

    /**
     * 文件类型(后缀名)
     */
    @Field("fileType")
    private String fileType;

    /**
     * 案件或证据Id
     */
    @Field("objectId")
    private String objectId;

    /**
     * 附件分类
     */
    @Field("attachCategory")
    private Integer attachCategory;
} 