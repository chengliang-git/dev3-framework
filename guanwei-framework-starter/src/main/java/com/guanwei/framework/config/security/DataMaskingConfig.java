package com.guanwei.framework.config.security;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

/**
 * 数据脱敏配置
 * 提供敏感数据脱敏功能
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "framework.security.data-masking", name = "enabled", havingValue = "true")
public class DataMaskingConfig {

    /**
     * 数据脱敏注解
     */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @JsonSerialize(using = DataMaskingSerializer.class)
    public @interface DataMasking {
        /**
         * 脱敏类型
         */
        MaskType type() default MaskType.CUSTOM;

        /**
         * 自定义脱敏规则
         */
        String pattern() default "";

        /**
         * 保留前几位
         */
        int prefix() default 0;

        /**
         * 保留后几位
         */
        int suffix() default 0;

        /**
         * 脱敏字符
         */
        String maskChar() default "*";
    }

    /**
     * 脱敏类型枚举
     */
    public enum MaskType {
        /**
         * 手机号脱敏
         */
        PHONE {
            @Override
            public String mask(String value) {
                if (value == null || value.length() < 7) {
                    return value;
                }
                return value.substring(0, 3) + "****" + value.substring(7);
            }
        },

        /**
         * 邮箱脱敏
         */
        EMAIL {
            @Override
            public String mask(String value) {
                if (value == null || !value.contains("@")) {
                    return value;
                }
                String[] parts = value.split("@");
                if (parts[0].length() <= 2) {
                    return value;
                }
                return parts[0].substring(0, 2) + "***@" + parts[1];
            }
        },

        /**
         * 身份证号脱敏
         */
        ID_CARD {
            @Override
            public String mask(String value) {
                if (value == null || value.length() < 8) {
                    return value;
                }
                return value.substring(0, 4) + "********" + value.substring(12);
            }
        },

        /**
         * 银行卡号脱敏
         */
        BANK_CARD {
            @Override
            public String mask(String value) {
                if (value == null || value.length() < 8) {
                    return value;
                }
                return value.substring(0, 4) + " **** **** " + value.substring(value.length() - 4);
            }
        },

        /**
         * 姓名脱敏
         */
        NAME {
            @Override
            public String mask(String value) {
                if (value == null || value.length() <= 1) {
                    return value;
                }
                return value.charAt(0) + "**";
            }
        },

        /**
         * 地址脱敏
         */
        ADDRESS {
            @Override
            public String mask(String value) {
                if (value == null || value.length() < 10) {
                    return value;
                }
                return value.substring(0, 6) + "****" + value.substring(value.length() - 4);
            }
        },

        /**
         * 自定义脱敏
         */
        CUSTOM {
            @Override
            public String mask(String value) {
                return value; // 由pattern处理
            }
        };

        /**
         * 脱敏方法
         */
        public abstract String mask(String value);
    }

    /**
     * 数据脱敏序列化器
     */
    public static class DataMaskingSerializer extends JsonSerializer<String> implements ContextualSerializer {

        private DataMasking annotation;

        public DataMaskingSerializer() {
        }

        public DataMaskingSerializer(DataMasking annotation) {
            this.annotation = annotation;
        }

        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }

            String maskedValue = maskValue(value);
            gen.writeString(maskedValue);
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
            if (property != null) {
                DataMasking annotation = property.getAnnotation(DataMasking.class);
                if (annotation != null) {
                    return new DataMaskingSerializer(annotation);
                }
            }
            return this;
        }

        /**
         * 脱敏处理
         */
        private String maskValue(String value) {
            if (annotation == null) {
                return value;
            }

            MaskType type = annotation.type();
            if (type == MaskType.CUSTOM) {
                return customMask(value);
            } else {
                return type.mask(value);
            }
        }

        /**
         * 自定义脱敏
         */
        private String customMask(String value) {
            String pattern = annotation.pattern();
            int prefix = annotation.prefix();
            int suffix = annotation.suffix();
            String maskChar = annotation.maskChar();

            if (pattern != null && !pattern.isEmpty()) {
                // 使用正则表达式脱敏
                return value.replaceAll(pattern, maskChar);
            } else if (prefix > 0 || suffix > 0) {
                // 使用前后缀脱敏
                return prefixSuffixMask(value, prefix, suffix, maskChar);
            }

            return value;
        }

        /**
         * 前后缀脱敏
         */
        private String prefixSuffixMask(String value, int prefix, int suffix, String maskChar) {
            if (value.length() <= prefix + suffix) {
                return value;
            }

            StringBuilder result = new StringBuilder();
            if (prefix > 0) {
                result.append(value.substring(0, prefix));
            }

            int maskLength = value.length() - prefix - suffix;
            for (int i = 0; i < maskLength; i++) {
                result.append(maskChar);
            }

            if (suffix > 0) {
                result.append(value.substring(value.length() - suffix));
            }

            return result.toString();
        }
    }

    /**
     * 数据脱敏工具类
     */
    @Bean
    public DataMaskingUtil dataMaskingUtil() {
        return new DataMaskingUtil();
    }

    /**
     * 数据脱敏工具类
     */
    public static class DataMaskingUtil {

        /**
         * 手机号脱敏
         */
        public String maskPhone(String phone) {
            return MaskType.PHONE.mask(phone);
        }

        /**
         * 邮箱脱敏
         */
        public String maskEmail(String email) {
            return MaskType.EMAIL.mask(email);
        }

        /**
         * 身份证号脱敏
         */
        public String maskIdCard(String idCard) {
            return MaskType.ID_CARD.mask(idCard);
        }

        /**
         * 银行卡号脱敏
         */
        public String maskBankCard(String bankCard) {
            return MaskType.BANK_CARD.mask(bankCard);
        }

        /**
         * 姓名脱敏
         */
        public String maskName(String name) {
            return MaskType.NAME.mask(name);
        }

        /**
         * 地址脱敏
         */
        public String maskAddress(String address) {
            return MaskType.ADDRESS.mask(address);
        }

        /**
         * 自定义脱敏
         */
        public String customMask(String value, String pattern, String maskChar) {
            if (value == null || pattern == null) {
                return value;
            }
            return value.replaceAll(pattern, maskChar);
        }
    }
}
