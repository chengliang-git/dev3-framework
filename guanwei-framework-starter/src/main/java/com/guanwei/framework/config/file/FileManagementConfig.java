package com.guanwei.framework.config.file;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.guanwei.framework.config.FrameworkProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件管理配置
 * 提供统一的文件上传下载管理功能，支持本地存储和S3存储
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "framework.file-management", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FileManagementConfig {

    private final FrameworkProperties frameworkProperties;

    public FileManagementConfig(FrameworkProperties frameworkProperties) {
        this.frameworkProperties = frameworkProperties;
        initializeDirectories();
    }

    /**
     * 初始化目录
     */
    private void initializeDirectories() {
        try {
            FrameworkProperties.FileManagement config = frameworkProperties.getFileManagement();
            
            // 如果是本地存储，创建目录
            if ("local".equals(config.getStorageType())) {
                // 创建上传目录
                Path uploadPath = Paths.get(config.getLocalPath());
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                    log.info("Created upload directory: {}", uploadPath);
                }
                
                // 创建临时目录
                Path tempPath = Paths.get(config.getTempPath());
                if (!Files.exists(tempPath)) {
                    Files.createDirectories(tempPath);
                    log.info("Created temp directory: {}", tempPath);
                }
            }
            
            log.info("File management directories initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize file management directories", e);
        }
    }

    /**
     * Amazon S3客户端配置
     */
    @Bean
    @ConditionalOnClass(AmazonS3.class)
    @ConditionalOnProperty(prefix = "framework.file-management", name = "storageType", havingValue = "s3")
    @ConditionalOnMissingBean(name = "amazonS3")
    public AmazonS3 amazonS3() {
        FrameworkProperties.FileManagement config = frameworkProperties.getFileManagement();
        FrameworkProperties.S3 s3Config = config.getS3();
        
        if (s3Config == null) {
            log.warn("S3 configuration not found, using default S3 client");
            return AmazonS3ClientBuilder.standard().build();
        }

        try {
            AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                    .withEndpointConfiguration(
                            new AwsClientBuilder.EndpointConfiguration(
                                    s3Config.getServiceURL(),
                                    s3Config.getRegion()
                            )
                    )
                    .withCredentials(
                            new AWSStaticCredentialsProvider(
                                    new BasicAWSCredentials(
                                            s3Config.getAccessKey(),
                                            s3Config.getSecretKey()
                                    )
                            )
                    )
                    .withPathStyleAccessEnabled(true);

            // 设置签名版本
            if (s3Config.getSignatureVersion() != null) {
                // 记录签名版本配置，但不使用不兼容的API
                log.info("S3 signature version configured: {} (API not available in current version)", s3Config.getSignatureVersion());
            }

            AmazonS3 s3Client = builder.build();
            log.info("Amazon S3 client initialized successfully for bucket: {}", s3Config.getBucketName());
            return s3Client;
        } catch (Exception e) {
            log.error("Failed to initialize Amazon S3 client", e);
            throw new RuntimeException("Failed to initialize Amazon S3 client", e);
        }
    }

    /**
     * 文件上传解析器
     */
    @Bean
    @ConditionalOnMissingBean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        log.info("Multipart resolver initialized");
        return resolver;
    }

    /**
     * 文件存储服务
     */
    @Bean
    @ConditionalOnMissingBean
    public FileStorageService fileStorageService() {
        log.info("File storage service initialized");
        return new FileStorageService(frameworkProperties, amazonS3());
    }

    /**
     * 文件处理服务
     */
    @Bean
    @ConditionalOnMissingBean
    public FileProcessingService fileProcessingService() {
        log.info("File processing service initialized");
        return new FileProcessingService(frameworkProperties);
    }

    /**
     * 文件上传服务
     */
    @Bean
    @ConditionalOnMissingBean
    public FileUploadService fileUploadService() {
        log.info("File upload service initialized");
        return new FileUploadService(frameworkProperties, fileStorageService(), fileProcessingService());
    }

    /**
     * 文件下载服务
     */
    @Bean
    @ConditionalOnMissingBean
    public FileDownloadService fileDownloadService() {
        log.info("File download service initialized");
        return new FileDownloadService(frameworkProperties, fileStorageService(), amazonS3());
    }
}
