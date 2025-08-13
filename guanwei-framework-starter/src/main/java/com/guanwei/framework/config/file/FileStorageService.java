package com.guanwei.framework.config.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.guanwei.framework.config.FrameworkProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * 文件存储服务
 * 提供统一的文件存储功能，支持本地存储和S3存储
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Service
public class FileStorageService {

    private final FrameworkProperties frameworkProperties;
    private final AmazonS3 amazonS3;

    @Autowired(required = false)
    public FileStorageService(FrameworkProperties frameworkProperties, AmazonS3 amazonS3) {
        this.frameworkProperties = frameworkProperties;
        this.amazonS3 = amazonS3;
    }

    /**
     * 存储文件
     */
    public FileStorageResult storeFile(MultipartFile file) throws IOException {
        FrameworkProperties.FileManagement config = frameworkProperties.getFileManagement();
        
        // 验证文件类型
        if (!isAllowedFileType(file.getOriginalFilename(), config.getAllowedExtensions())) {
            throw new IllegalArgumentException("File type not allowed: " + file.getOriginalFilename());
        }
        
        // 验证文件大小
        if (file.getSize() > config.getMaxFileSize()) {
            throw new IllegalArgumentException("File size exceeds limit: " + file.getSize());
        }
        
        String fileName = generateFileName(file.getOriginalFilename());
        
        if ("s3".equals(config.getStorageType()) && amazonS3 != null) {
            return storeFileToS3(file, fileName);
        } else {
            return storeFileToLocal(file, fileName);
        }
    }

    /**
     * 存储文件到S3
     */
    private FileStorageResult storeFileToS3(MultipartFile file, String fileName) throws IOException {
        FrameworkProperties.S3 s3Config = frameworkProperties.getFileManagement().getS3();
        
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            
            PutObjectRequest request = new PutObjectRequest(
                    s3Config.getBucketName(),
                    fileName,
                    file.getInputStream(),
                    metadata
            );
            
            amazonS3.putObject(request);
            
            String fileUrl = s3Config.getUrlPrefix() + "/" + fileName;
            if (s3Config.isPreSignedURL()) {
                fileUrl = amazonS3.generatePresignedUrl(
                        s3Config.getBucketName(),
                        fileName,
                        java.util.Date.from(java.time.Instant.now().plusSeconds(s3Config.getPreSignedExpiry() * 3600))
                ).toString();
            }
            
            log.info("File stored to S3 successfully: {}", fileName);
            return new FileStorageResult(fileName, fileUrl, "s3", file.getSize());
            
        } catch (Exception e) {
            log.error("Failed to store file to S3: {}", fileName, e);
            throw new RuntimeException("Failed to store file to S3", e);
        }
    }

    /**
     * 存储文件到本地
     */
    private FileStorageResult storeFileToLocal(MultipartFile file, String fileName) throws IOException {
        FrameworkProperties.FileManagement config = frameworkProperties.getFileManagement();
        Path targetPath = Paths.get(config.getLocalPath(), fileName);
        
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        String fileUrl = "/files/" + fileName;
        log.info("File stored to local successfully: {}", fileName);
        return new FileStorageResult(fileName, fileUrl, "local", file.getSize());
    }

    /**
     * 获取文件
     */
    public InputStream getFile(String fileName) throws IOException {
        FrameworkProperties.FileManagement config = frameworkProperties.getFileManagement();
        
        if ("s3".equals(config.getStorageType()) && amazonS3 != null) {
            return getFileFromS3(fileName);
        } else {
            return getFileFromLocal(fileName);
        }
    }

    /**
     * 从S3获取文件
     */
    private InputStream getFileFromS3(String fileName) {
        FrameworkProperties.S3 s3Config = frameworkProperties.getFileManagement().getS3();
        
        try {
            S3Object s3Object = amazonS3.getObject(s3Config.getBucketName(), fileName);
            return s3Object.getObjectContent();
        } catch (Exception e) {
            log.error("Failed to get file from S3: {}", fileName, e);
            throw new RuntimeException("Failed to get file from S3", e);
        }
    }

    /**
     * 从本地获取文件
     */
    private InputStream getFileFromLocal(String fileName) throws IOException {
        FrameworkProperties.FileManagement config = frameworkProperties.getFileManagement();
        Path filePath = Paths.get(config.getLocalPath(), fileName);
        
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + fileName);
        }
        
        return new FileInputStream(filePath.toFile());
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String fileName) {
        FrameworkProperties.FileManagement config = frameworkProperties.getFileManagement();
        
        if ("s3".equals(config.getStorageType()) && amazonS3 != null) {
            return deleteFileFromS3(fileName);
        } else {
            return deleteFileFromLocal(fileName);
        }
    }

    /**
     * 从S3删除文件
     */
    private boolean deleteFileFromS3(String fileName) {
        FrameworkProperties.S3 s3Config = frameworkProperties.getFileManagement().getS3();
        
        try {
            amazonS3.deleteObject(s3Config.getBucketName(), fileName);
            log.info("File deleted from S3 successfully: {}", fileName);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", fileName, e);
            return false;
        }
    }

    /**
     * 从本地删除文件
     */
    private boolean deleteFileFromLocal(String fileName) {
        FrameworkProperties.FileManagement config = frameworkProperties.getFileManagement();
        Path filePath = Paths.get(config.getLocalPath(), fileName);
        
        try {
            Files.deleteIfExists(filePath);
            log.info("File deleted from local successfully: {}", fileName);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete file from local: {}", fileName, e);
            return false;
        }
    }

    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String fileName) {
        FrameworkProperties.FileManagement config = frameworkProperties.getFileManagement();
        
        if ("s3".equals(config.getStorageType()) && amazonS3 != null) {
            return fileExistsInS3(fileName);
        } else {
            return fileExistsInLocal(fileName);
        }
    }

    /**
     * 检查S3中文件是否存在
     */
    private boolean fileExistsInS3(String fileName) {
        FrameworkProperties.S3 s3Config = frameworkProperties.getFileManagement().getS3();
        return amazonS3.doesObjectExist(s3Config.getBucketName(), fileName);
    }

    /**
     * 检查本地文件是否存在
     */
    private boolean fileExistsInLocal(String fileName) {
        FrameworkProperties.FileManagement config = frameworkProperties.getFileManagement();
        Path filePath = Paths.get(config.getLocalPath(), fileName);
        return Files.exists(filePath);
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * 检查文件类型是否允许
     */
    private boolean isAllowedFileType(String fileName, List<String> allowedExtensions) {
        if (fileName == null || allowedExtensions == null || allowedExtensions.isEmpty()) {
            return false;
        }
        
        final String extension;
        if (fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        } else {
            extension = "";
        }
        
        return allowedExtensions.stream()
                .anyMatch(ext -> ext.toLowerCase().equals(extension));
    }

    /**
     * 文件存储结果
     */
    public static class FileStorageResult {
        private final String fileName;
        private final String fileUrl;
        private final String storageType;
        private final long fileSize;

        public FileStorageResult(String fileName, String fileUrl, String storageType, long fileSize) {
            this.fileName = fileName;
            this.fileUrl = fileUrl;
            this.storageType = storageType;
            this.fileSize = fileSize;
        }

        public String getFileName() { return fileName; }
        public String getFileUrl() { return fileUrl; }
        public String getStorageType() { return storageType; }
        public long getFileSize() { return fileSize; }
    }
}
