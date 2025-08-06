package com.guanwei.tles.casetransfer.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Jackson配置类
 * 配置全局的ObjectMapper支持LocalDateTime处理
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class JacksonConfig {

    /**
     * 自定义LocalDateTime反序列化器，处理带时区的ISO 8601格式
     */
    public static class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter[] FORMATTERS = {
                DateTimeFormatter.ISO_DATE_TIME, // 2025-07-02T10:49:00+08:00
                DateTimeFormatter.ISO_LOCAL_DATE_TIME, // 2025-07-02T10:49:00
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS")
        };

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            if (value == null || value.isEmpty()) {
                return null;
            }

            try {
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(value);
                LocalDateTime result = zonedDateTime.toLocalDateTime();
                return result;
            } catch (DateTimeParseException e) {
                // 如果不是带时区的格式，尝试其他格式
                for (DateTimeFormatter formatter : FORMATTERS) {
                    try {
                        LocalDateTime result = LocalDateTime.parse(value, formatter);
                        return result;
                    } catch (DateTimeParseException ignored) {
                        // 继续尝试下一个格式
                    }
                }
                log.error("Failed to parse LocalDateTime with any formatter: {}", value);
                throw new IOException("Unable to parse LocalDateTime: " + value, e);
            }
        }
    }

    /**
     * 配置全局的ObjectMapper
     * 支持多种日期时间格式，包括带时区的ISO 8601格式
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 配置ObjectMapper以支持多种日期时间格式
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 配置JavaTimeModule以支持多种日期时间格式
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 使用自定义的LocalDateTime反序列化器
        javaTimeModule.addDeserializer(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());

        // 注册序列化器
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ISO_DATE_TIME,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS")
        };

        for (DateTimeFormatter formatter : formatters) {
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        }

        objectMapper.registerModule(javaTimeModule);
        
        log.info("Global ObjectMapper configured with LocalDateTime support");
        return objectMapper;
    }
} 