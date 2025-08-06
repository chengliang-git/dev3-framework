package com.guanwei.tles.casetransfer.entity;

import lombok.Data;
// import org.springframework.data.mongodb.core.mapping.Field; // 暂时禁用MongoDB

import java.time.LocalDateTime;

/**
 * 案件文件实体类 - MongoDB功能暂时禁用
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Data
public class CaseDocument {

    /**
     * 文件ID
     */
    // @Field("documentId") // 暂时禁用MongoDB
    private String documentId;

    /**
     * 文件名
     */
    // @Field("fileName") // 暂时禁用MongoDB
    private String fileName;

    /**
     * 文件路径
     */
    // @Field("filePath") // 暂时禁用MongoDB
    private String filePath;

    /**
     * 文件大小（字节）
     */
    // @Field("fileSize") // 暂时禁用MongoDB
    private Long fileSize;

    /**
     * 文件类型
     */
    // @Field("fileType") // 暂时禁用MongoDB
    private String fileType;

    /**
     * 文件描述
     */
    // @Field("description") // 暂时禁用MongoDB
    private String description;

    /**
     * 上传时间
     */
    // @Field("uploadTime") // 暂时禁用MongoDB
    private LocalDateTime uploadTime;

    /**
     * 上传人
     */
    // @Field("uploader") // 暂时禁用MongoDB
    private String uploader;
} 