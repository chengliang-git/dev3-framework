package com.guanwei.framework.config.file;

import com.amazonaws.services.s3.AmazonS3;
import com.guanwei.framework.config.FrameworkProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 文件下载服务
 * 提供文件下载功能
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Service
public class FileDownloadService {

    private final FrameworkProperties frameworkProperties;
    private final FileStorageService fileStorageService;
    private final AmazonS3 amazonS3;

    @Autowired(required = false)
    public FileDownloadService(FrameworkProperties frameworkProperties, 
                             FileStorageService fileStorageService,
                             AmazonS3 amazonS3) {
        this.frameworkProperties = frameworkProperties;
        this.fileStorageService = fileStorageService;
        this.amazonS3 = amazonS3;
    }

    /**
     * 下载文件
     */
    public ResponseEntity<Resource> downloadFile(String fileName) throws IOException {
        log.info("Starting file download: {}", fileName);
        
        // 检查文件是否存在
        if (!fileStorageService.fileExists(fileName)) {
            throw new IOException("File not found: " + fileName);
        }
        
        // 获取文件流
        InputStream inputStream = fileStorageService.getFile(fileName);
        
        // 创建资源
        InputStreamResource resource = new InputStreamResource(inputStream);
        
        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);
        
        // 设置缓存控制
        headers.setCacheControl("no-cache");
        headers.setPragma("no-cache");
        headers.setExpires(0);
        
        log.info("File download completed: {}", fileName);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    /**
     * 获取文件预览
     */
    public ResponseEntity<Resource> previewFile(String fileName) throws IOException {
        log.info("Starting file preview: {}", fileName);
        
        // 检查文件是否存在
        if (!fileStorageService.fileExists(fileName)) {
            throw new IOException("File not found: " + fileName);
        }
        
        // 获取文件流
        InputStream inputStream = fileStorageService.getFile(fileName);
        
        // 创建资源
        InputStreamResource resource = new InputStreamResource(inputStream);
        
        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("inline", fileName);
        
        log.info("File preview completed: {}", fileName);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    /**
     * 生成预签名下载URL（S3）
     */
    public String generatePresignedDownloadUrl(String fileName) {
        FrameworkProperties.FileManagement config = frameworkProperties.getFileManagement();
        
        if (!"s3".equals(config.getStorageType()) || amazonS3 == null) {
            log.warn("Presigned URL generation is only supported for S3 storage");
            return null;
        }
        
        try {
            FrameworkProperties.S3 s3Config = config.getS3();
            Date expiration = new Date(System.currentTimeMillis() + s3Config.getPreSignedExpiry() * 3600 * 1000L);
            
            String presignedUrl = amazonS3.generatePresignedUrl(
                    s3Config.getBucketName(),
                    fileName,
                    expiration
            ).toString();
            
            log.info("Generated presigned download URL for file: {}", fileName);
            return presignedUrl;
            
        } catch (Exception e) {
            log.error("Failed to generate presigned download URL for file: {}", fileName, e);
            return null;
        }
    }

    /**
     * 获取文件信息
     */
    public FileInfo getFileInfo(String fileName) throws IOException {
        log.info("Getting file info: {}", fileName);
        
        // 检查文件是否存在
        if (!fileStorageService.fileExists(fileName)) {
            throw new IOException("File not found: " + fileName);
        }
        
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(fileName);
        fileInfo.setExists(true);
        
        // 获取文件大小（如果可能）
        try {
            // 这里可以实现获取文件大小的逻辑
            fileInfo.setFileSize(0L); // 暂时设为0
        } catch (Exception e) {
            log.warn("Failed to get file size for: {}", fileName, e);
        }
        
        // 生成下载URL
        FrameworkProperties.FileManagement config = frameworkProperties.getFileManagement();
        if ("s3".equals(config.getStorageType()) && amazonS3 != null) {
            fileInfo.setDownloadUrl(generatePresignedDownloadUrl(fileName));
        } else {
            fileInfo.setDownloadUrl("/files/download/" + fileName);
        }
        
        log.info("File info retrieved: {}", fileName);
        return fileInfo;
    }

    /**
     * 文件信息
     */
    public static class FileInfo {
        private String fileName;
        private boolean exists;
        private long fileSize;
        private String downloadUrl;
        private String contentType;

        // Getters and Setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public boolean isExists() { return exists; }
        public void setExists(boolean exists) { this.exists = exists; }
        
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        
        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
        
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
    }
}
