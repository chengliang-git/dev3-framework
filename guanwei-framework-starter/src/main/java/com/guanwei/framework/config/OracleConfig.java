package com.guanwei.framework.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Oracle数据库配置
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Configuration
public class OracleConfig {



    /**
     * 自动填充处理器
     */
    @Component
    public static class MyMetaObjectHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            // 设置创建时间
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
            // 设置修改时间
            this.strictInsertFill(metaObject, "modifyTime", LocalDateTime.class, LocalDateTime.now());
            // 设置创建人（可以从SecurityContext中获取当前用户）
            this.strictInsertFill(metaObject, "creator", String.class, getCurrentUsername());
            // 设置修改人
            this.strictInsertFill(metaObject, "modifier", String.class, getCurrentUsername());
            // 设置删除标记
            this.strictInsertFill(metaObject, "delFlag", Integer.class, 0);
            // 顺序号由前端提供，不自动填充
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            // 设置修改时间
            this.strictUpdateFill(metaObject, "modifyTime", LocalDateTime.class, LocalDateTime.now());
            // 设置修改人
            this.strictUpdateFill(metaObject, "modifier", String.class, getCurrentUsername());
        }

        /**
         * 获取当前用户名
         */
        private String getCurrentUsername() {
            try {
                // 这里可以从SecurityContext中获取当前登录用户
                // 暂时返回默认值，实际使用时需要根据具体的安全框架实现
                return "system";
            } catch (Exception e) {
                return "system";
            }
        }
    }
}