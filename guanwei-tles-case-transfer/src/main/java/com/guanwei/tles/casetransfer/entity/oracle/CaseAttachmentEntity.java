package com.guanwei.tles.casetransfer.entity.oracle;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanwei.framework.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * Oracle案件附件实体类 - 对应CaseAttachment.cs
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("LE_CaseAttachment")
public class CaseAttachmentEntity extends BaseEntity {

    /**
     * 附件Id
     */
    private String attachId;

    /**
     * 附件名称
     */
    private String attachName;

    /**
     * 相对路径
     */
    private String filePath;

    /**
     * 文件大小(KB)
     */
    private BigDecimal fileSize;

    /**
     * 文件类型(后缀名)
     */
    private String fileType;

    /**
     * 案件或证据Id
     */
    private String objectId;

    /**
     * 附件分类
     */
    private Integer attachCategory;
} 