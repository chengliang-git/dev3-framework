package com.enterprise.framework.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT工具类
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Component
public class JwtTokenUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 生成Token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @return Token
     */
    public String generateToken(Long userId, String username) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtProperties.getExpiration());
        
        return JWT.create()
                .withSubject(username)
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withIssuedAt(new Date())
                .withExpiresAt(expiryDate)
                .sign(Algorithm.HMAC256(jwtProperties.getSecret()));
    }

    /**
     * 验证Token
     *
     * @param token Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            log.warn("JWT验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取用户名
     *
     * @param token Token
     * @return 用户名
     */
    public String getUsername(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("username").asString();
        } catch (Exception e) {
            log.error("获取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取用户ID
     *
     * @param token Token
     * @return 用户ID
     */
    public Long getUserId(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("userId").asLong();
        } catch (Exception e) {
            log.error("获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取Token过期时间
     *
     * @param token Token
     * @return 过期时间
     */
    public Date getExpirationDate(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getExpiresAt();
        } catch (Exception e) {
            log.error("获取Token过期时间失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断Token是否过期
     *
     * @param token Token
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        Date expirationDate = getExpirationDate(token);
        return expirationDate != null && expirationDate.before(new Date());
    }

    /**
     * 刷新Token
     *
     * @param token 原Token
     * @return 新Token
     */
    public String refreshToken(String token) {
        try {
            Long userId = getUserId(token);
            String username = getUsername(token);
            return generateToken(userId, username);
        } catch (Exception e) {
            log.error("刷新Token失败: {}", e.getMessage());
            return null;
        }
    }
}