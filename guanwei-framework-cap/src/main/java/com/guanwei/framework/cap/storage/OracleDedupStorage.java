package com.guanwei.framework.cap.storage;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 基于 Oracle 的幂等去重存储
 * 依赖唯一键约束实现 SETNX 语义
 */
public class OracleDedupStorage implements DedupStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final String TABLE = "CAP_DEDUP";

    public OracleDedupStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        createTableIfNeeded();
    }

    @Override
    public boolean tryMarkProcessed(String key, long ttlSeconds) {
        try {
            LocalDateTime expiresAt = ttlSeconds > 0 ? LocalDateTime.now().plusSeconds(ttlSeconds) : null;
            String sql = "INSERT INTO " + TABLE + " (IDEMPOTENT_KEY, EXPIRESAT) VALUES (?, ?)";
            jdbcTemplate.update(sql, key, expiresAt != null ? Timestamp.valueOf(expiresAt) : null);
            return true;
        } catch (Exception e) {
            // 唯一约束冲突即为已存在，视为重复
            return false;
        }
    }

    private void createTableIfNeeded() {
        try {
            String sql = "CREATE TABLE " + TABLE + " (\n" +
                    "  IDEMPOTENT_KEY VARCHAR2(256) PRIMARY KEY,\n" +
                    "  EXPIRESAT TIMESTAMP NULL\n" +
                    ")";
            jdbcTemplate.execute(sql);
        } catch (Exception ignored) {
        }

        // 可选：为过期字段创建索引以便清理
        try {
            jdbcTemplate.execute("CREATE INDEX IDX_CAP_DEDUP_EXPIRES ON " + TABLE + "(EXPIRESAT)");
        } catch (Exception ignored) {
        }
    }
}


