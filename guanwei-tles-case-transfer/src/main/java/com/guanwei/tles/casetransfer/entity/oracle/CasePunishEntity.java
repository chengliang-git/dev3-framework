package com.guanwei.tles.casetransfer.entity.oracle;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanwei.framework.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Oracle案件处罚实体类 - 对应CasePunish.cs
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("LE_CasePunish")
public class CasePunishEntity extends BaseEntity {

    /**
     * 处罚Id
     */
    private String punishId;

    /**
     * 是否当场处罚
     */
    private Integer isSpotPunish;

    /**
     * 是否当场收缴罚款
     */
    private Integer isCollectFine;

    /**
     * 收缴罚款原因
     */
    private String collectFineReason;

    /**
     * 处罚结论
     */
    private Integer punishResult;

    /**
     * 处罚依据
     */
    private String punishBasis;

    /**
     * 案件Id
     */
    private String caseId;

    /**
     * 处罚来源
     */
    private Integer source;

    /**
     * 处罚情形主键
     */
    private String situId;

    /**
     * 是否有减轻处罚
     */
    private Integer isMitigate;

    /**
     * 减轻处罚依据
     */
    private String mitigateBasis;

    /**
     * 减轻处罚责任人
     */
    private String respPerson;

    /**
     * 处罚内容
     */
    private String punishContent;

    /**
     * 删除标记
     */
    private Integer delFlag;
} 