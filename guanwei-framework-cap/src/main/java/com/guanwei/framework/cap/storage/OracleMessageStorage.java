package com.guanwei.framework.cap.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guanwei.framework.cap.CapMessage;
import com.guanwei.framework.cap.CapMessageStatus;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.guanwei.framework.cap.util.MessageIdGenerator;

/**
 * Oracle 消息存储实现
 * 完整实现，参考 .NET Core CAP 的 Oracle 存储实现
 */
@Slf4j
public class OracleMessageStorage implements MessageStorage {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // TransactionTemplate optional, reserved for future transactional enhancements

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 表名常量
    private static final String PUBLISHED_TABLE = "CAP_PUBLISHED";
    private static final String RECEIVED_TABLE = "CAP_RECEIVED";
    private static final String LOCK_TABLE = "CAP_LOCKS";

    /**
     * 初始化表结构
     */
    @PostConstruct
    public void initializeTables() {
        createPublishedTable();
        createReceivedTable();
        createLockTable();
    }

    private void createPublishedTable() {
        String sql = """
                CREATE TABLE CAP_PUBLISHED (
                    ID NUMERIC(19) PRIMARY KEY,
                    NAME VARCHAR2(200) NOT NULL,
                    CONTENT CLOB,
                    RETRIES NUMBER(10) DEFAULT 0,
                    STATUSNAME VARCHAR2(50),
                    EXPIRESAT TIMESTAMP,
                    ADDED TIMESTAMP DEFAULT SYSTIMESTAMP,
                    VERSION VARCHAR2(20) DEFAULT 'v1'
                )
                """;
        try {
            jdbcTemplate.execute(sql);
            log.info("Created CAP_PUBLISHED table");
        } catch (Exception e) {
            log.debug("CAP_PUBLISHED table may already exist: {}", e.getMessage());
        }
    }

    private void createReceivedTable() {
        String sql = """
                CREATE TABLE CAP_RECEIVED (
                    ID NUMERIC(19) PRIMARY KEY,
                    NAME VARCHAR2(200) NOT NULL,
                    SUBGROUP VARCHAR2(200),
                    CONTENT CLOB,
                    RETRIES NUMBER(10) DEFAULT 0,
                    STATUSNAME VARCHAR2(50),
                    EXPIRESAT TIMESTAMP,
                    ADDED TIMESTAMP DEFAULT SYSTIMESTAMP,
                    VERSION VARCHAR2(20) DEFAULT 'v1'
                )
                """;
        try {
            jdbcTemplate.execute(sql);
            log.info("Created CAP_RECEIVED table");
        } catch (Exception e) {
            log.debug("CAP_RECEIVED table may already exist: {}", e.getMessage());
        }
    }

    private void createLockTable() {
        String sql = """
                CREATE TABLE CAP_LOCKS (
                    KEYID VARCHAR2(128) PRIMARY KEY,
                    INSTANCE VARCHAR2(256) NOT NULL,
                    LASTLOCKTIME TIMESTAMP NOT NULL
                )
                """;
        try {
            jdbcTemplate.execute(sql);
            log.info("Created CAP_LOCKS table");
        } catch (Exception e) {
            log.debug("CAP_LOCKS table may already exist: {}", e.getMessage());
        }
    }

    @Override
    public CompletableFuture<Boolean> acquireLockAsync(String key, Duration ttl, String instance) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 先清理过期的锁
                String cleanupSql = "DELETE FROM " + LOCK_TABLE + " WHERE LASTLOCKTIME < SYSTIMESTAMP";
                jdbcTemplate.update(cleanupSql);

                // 尝试插入新锁
                String insertSql = "INSERT INTO " + LOCK_TABLE + " (KEYID, INSTANCE, LASTLOCKTIME) VALUES (?, ?, ?)";
                jdbcTemplate.update(insertSql, key, instance,
                        Timestamp.valueOf(LocalDateTime.now().plus(ttl)));
                return true;
            } catch (Exception e) {
                log.debug("Failed to acquire lock: {} by instance: {}", key, instance);
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Void> releaseLockAsync(String key, String instance) {
        return CompletableFuture.runAsync(() -> {
            try {
                String sql = "DELETE FROM " + LOCK_TABLE + " WHERE KEYID = ? AND INSTANCE = ?";
                jdbcTemplate.update(sql, key, instance);
            } catch (Exception e) {
                log.error("Error releasing lock: {}", key, e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> renewLockAsync(String key, Duration ttl, String instance) {
        return CompletableFuture.runAsync(() -> {
            try {
                String sql = "UPDATE " + LOCK_TABLE + " SET LASTLOCKTIME = ? WHERE KEYID = ? AND INSTANCE = ?";
                jdbcTemplate.update(sql,
                        Timestamp.valueOf(LocalDateTime.now().plus(ttl)), key, instance);
            } catch (Exception e) {
                log.error("Error renewing lock: {}", key, e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> changePublishStateToDelayedAsync(List<Long> ids) {
        return CompletableFuture.runAsync(() -> {
            try {
                String sql = "UPDATE " + PUBLISHED_TABLE + " SET STATUSNAME = ? WHERE ID = ?";
                for (Long id : ids) {
                    jdbcTemplate.update(sql, CapMessageStatus.DELAYED.getValue(), id);
                }
            } catch (Exception e) {
                log.error("Error changing publish state to delayed", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> changePublishStateAsync(CapMessage message, CapMessageStatus status,
            Object transaction) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (message != null && message.getId() != null) {
                    String sql = "UPDATE " + PUBLISHED_TABLE + " SET STATUSNAME = ? WHERE ID = ?";
                    jdbcTemplate.update(sql, status.getValue(), message.getId());
                }
            } catch (Exception e) {
                log.error("Error changing publish state", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> changeReceiveStateAsync(CapMessage message, CapMessageStatus status) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (message != null && message.getId() != null) {
                    String sql = "UPDATE " + RECEIVED_TABLE + " SET STATUSNAME = ? WHERE ID = ?";
                    jdbcTemplate.update(sql, status.getValue(), message.getId());
                }
            } catch (Exception e) {
                log.error("Error changing receive state", e);
            }
        });
    }

    @Override
    public CompletableFuture<CapMessage> storeMessageAsync(String name, Object content, Object transaction) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CapMessage message;
                Long id;
                if (content instanceof CapMessage) {
                    message = (CapMessage) content;
                    id = message.getId() != null ? message.getId() : generateMessageId();
                } else {
                    message = new CapMessage(name, content);
                    id = generateMessageId();
                    message.setDbId(id);
                }

                String contentJson = objectMapper.writeValueAsString(message.getContent());

                String sql = """
                        INSERT INTO %s (ID, NAME, CONTENT, RETRIES, STATUSNAME, ADDED, VERSION)
                        VALUES (?, ?, ?, ?, ?, SYSTIMESTAMP, ?)
                        """.formatted(PUBLISHED_TABLE);

                jdbcTemplate.update(sql, id, name, contentJson, 0,
                        CapMessageStatus.SCHEDULED.getValue(), "v1");

                message.setDbId(id);
                message.setStatus(CapMessageStatus.SCHEDULED);
                message.setAdded(LocalDateTime.now());
                message.setRetries(0);

                return message;
            } catch (Exception e) {
                log.error("Error storing published message", e);
                throw new RuntimeException("Failed to store published message", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> storeReceivedExceptionMessageAsync(String name, String group, String content) {
        return CompletableFuture.runAsync(() -> {
            try {
                Long id = generateMessageId();
                String sql = """
                        INSERT INTO %s (ID, NAME, GROUP_NAME, CONTENT, RETRIES, STATUSN, ADDED, VERSION)
                        VALUES (?, ?, ?, ?, ?, ?, SYSTIMESTAMP, ?)
                        """.formatted(RECEIVED_TABLE);

                jdbcTemplate.update(sql, id, name, group, content, 0,
                        CapMessageStatus.FAILED.getValue(), "v1");

            } catch (Exception e) {
                log.error("Error storing exception message", e);
            }
        });
    }

    @Override
    public CompletableFuture<CapMessage> storeReceivedMessageAsync(String name, String group, Object content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Long id = generateMessageId();
                String contentJson = objectMapper.writeValueAsString(content);

                String sql = """
                        INSERT INTO %s (ID, NAME, SUBGROUP, CONTENT, RETRIES, STATUSNAME, ADDED, VERSION)
                        VALUES (?, ?, ?, ?, ?, ?, SYSTIMESTAMP, ?)
                        """.formatted(RECEIVED_TABLE);

                jdbcTemplate.update(sql, id, name, group, contentJson, 0,
                        CapMessageStatus.SCHEDULED.getValue(), "v1");

                CapMessage message = new CapMessage(name, group, content);
                message.setDbId(id);
                message.setStatus(CapMessageStatus.SCHEDULED);
                message.setAdded(LocalDateTime.now());
                message.setRetries(0);

                return message;
            } catch (Exception e) {
                log.error("Error storing received message", e);
                throw new RuntimeException("Failed to store received message", e);
            }
        });
    }

    @Override
    public CompletableFuture<Integer> deleteExpiresAsync(String table, LocalDateTime timeout, int batchCount) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String tableName = "published".equals(table) ? PUBLISHED_TABLE : RECEIVED_TABLE;
                String sql = "DELETE FROM " + tableName + " WHERE ADDED < ? AND ROWNUM <= ?";
                int deleted = jdbcTemplate.update(sql, Timestamp.valueOf(timeout), batchCount);
                return deleted;
            } catch (Exception e) {
                log.error("Error deleting expired messages", e);
                return 0;
            }
        });
    }

    @Override
    public CompletableFuture<List<CapMessage>> getPublishedMessagesOfNeedRetry(Duration lookbackSeconds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LocalDateTime cutoff = LocalDateTime.now().minus(lookbackSeconds);
                String sql = """
                        SELECT ID, NAME, CONTENT, RETRIES, STATUSNAME, EXPIRESAT, ADDED, VERSION
                        FROM %s WHERE STATUSNAME = ? AND ADDED > ? ORDER BY ADDED
                        """.formatted(PUBLISHED_TABLE);

                return jdbcTemplate.query(sql, new CapMessageRowMapper(),
                        CapMessageStatus.FAILED.getValue(), Timestamp.valueOf(cutoff));
            } catch (Exception e) {
                log.error("Error getting published messages of need retry", e);
                return new java.util.ArrayList<>();
            }
        });
    }

    @Override
    public CompletableFuture<List<CapMessage>> getReceivedMessagesOfNeedRetry(Duration lookbackSeconds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LocalDateTime cutoff = LocalDateTime.now().minus(lookbackSeconds);
                String sql = """
                        SELECT ID, NAME, SUBGROUP, CONTENT, RETRIES, STATUSNAME, EXPIRESAT, ADDED, VERSION
                        FROM %s WHERE STATUSNAME = ? AND ADDED > ? ORDER BY ADDED
                        """.formatted(RECEIVED_TABLE);

                return jdbcTemplate.query(sql, new CapMessageRowMapper(),
                        CapMessageStatus.FAILED.getValue(), Timestamp.valueOf(cutoff));
            } catch (Exception e) {
                log.error("Error getting received messages of need retry", e);
                return new java.util.ArrayList<>();
            }
        });
    }

    @Override
    public CompletableFuture<Integer> deleteReceivedMessageAsync(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "DELETE FROM " + RECEIVED_TABLE + " WHERE ID = ?";
                int deleted = jdbcTemplate.update(sql, id);
                return deleted;
            } catch (Exception e) {
                log.error("Error deleting received message: {}", id, e);
                return 0;
            }
        });
    }

    @Override
    public CompletableFuture<Integer> deletePublishedMessageAsync(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "DELETE FROM " + PUBLISHED_TABLE + " WHERE ID = ?";
                int deleted = jdbcTemplate.update(sql, id);
                return deleted;
            } catch (Exception e) {
                log.error("Error deleting published message: {}", id, e);
                return 0;
            }
        });
    }

    @Override
    public CompletableFuture<Void> scheduleMessagesOfDelayedAsync(DelayedMessageScheduler scheduleTask) {
        return CompletableFuture.runAsync(() -> {
            try {
                String sql = """
                        SELECT ID, NAME, CONTENT, RETRIES, STATUSNAME, EXPIRESAT, ADDED, VERSION
                        FROM %s WHERE STATUSNAME = ? AND EXPIRESAT < SYSTIMESTAMP
                        """.formatted(PUBLISHED_TABLE);

                List<CapMessage> delayedMessages = jdbcTemplate.query(sql, new CapMessageRowMapper(),
                        CapMessageStatus.DELAYED.getValue());

                if (!delayedMessages.isEmpty()) {
                    scheduleTask.schedule(null, delayedMessages);
                }
            } catch (Exception e) {
                log.error("Error scheduling delayed messages", e);
            }
        });
    }

    @Override
    public CompletableFuture<Integer> deleteExpiredMessagesAsync(CapMessageStatus status, long expiredBefore) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LocalDateTime expiredTime = LocalDateTime.ofEpochSecond(expiredBefore, 0, java.time.ZoneOffset.UTC);
                int totalDeleted = 0;

                // 删除已发布消息
                String publishedSql = "DELETE FROM " + PUBLISHED_TABLE + " WHERE STATUSNAME = ? AND EXPIRES_AT < ?";
                int publishedDeleted = jdbcTemplate.update(publishedSql, status.getValue(),
                        Timestamp.valueOf(expiredTime));
                totalDeleted += publishedDeleted;

                // 删除已接收消息
                String receivedSql = "DELETE FROM " + RECEIVED_TABLE + " WHERE STATUSNAME = ? AND EXPIRES_AT < ?";
                int receivedDeleted = jdbcTemplate.update(receivedSql, status.getValue(),
                        Timestamp.valueOf(expiredTime));
                totalDeleted += receivedDeleted;

                return totalDeleted;
            } catch (Exception e) {
                log.error("Error deleting expired messages", e);
                return 0;
            }
        });
    }

    @Override
    public CompletableFuture<Void> updateStatusAsync(Long messageId, CapMessageStatus status) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 尝试更新已发布消息
                String publishedSql = "UPDATE " + PUBLISHED_TABLE + " SET STATUSNAME = ? WHERE ID = ?";
                int publishedUpdated = jdbcTemplate.update(publishedSql, status.getValue(), messageId);
                if (publishedUpdated > 0) {
                    return;
                }
            } catch (Exception e) {
                log.error("Error updating message status: {}", messageId, e);
            }
        });
    }

    private Long generateMessageId() {
        return MessageIdGenerator.getInstance().nextId();
    }

    private static class CapMessageRowMapper implements RowMapper<CapMessage> {
        @Override
        public CapMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            CapMessage message = new CapMessage();
            message.setDbId(rs.getLong("ID"));
            message.setName(rs.getString("NAME"));
            message.setContent(rs.getString("CONTENT"));
            message.setRetries(rs.getInt("RETRIES"));
            message.setStatus(CapMessageStatus.fromValue(rs.getString("STATUSNAME")));

            Timestamp expiresAt = rs.getTimestamp("EXPIRESAT");
            if (expiresAt != null) {
                message.setExpiresAt(expiresAt.toLocalDateTime());
            }

            Timestamp added = rs.getTimestamp("ADDED");
            if (added != null) {
                message.setAdded(added.toLocalDateTime());
            }

            message.setVersion(rs.getString("VERSION"));
            return message;
        }
    }
}