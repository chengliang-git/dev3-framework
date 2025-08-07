package com.guanwei.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.guanwei.framework.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * OAuth2客户端实体类
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("auth_oauth2_client")
public class OAuth2Client extends BaseEntity {

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 客户端密钥
     */
    private String clientSecret;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 重定向URI
     */
    private String redirectUri;

    /**
     * 授权范围
     */
    private String scope;

    /**
     * 授权类型
     */
    private String grantType;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
}
