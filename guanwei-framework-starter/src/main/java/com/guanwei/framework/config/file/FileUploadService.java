package com.guanwei.framework.config.file;

import com.guanwei.framework.config.FrameworkProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传服务
 * 提供文件上传功能
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@Service
public class FileUploadService {

    private final FrameworkProperties frameworkProperties;
    private final FileStorageService fileStorageService;
    private final FileProcessingService fileProcessingService;

    @Autowired
    public FileUploadService(FrameworkProperties frameworkProperties, 
                           FileStorageService fileStorageService,
                           FileProcessingService fileProcessingService) {
        this.frameworkProperties = frameworkProperties;
        this.fileStorageService = fileStorageService;
        this.fileProcessingService = fileProcessingService;
    }

    /**
     * 上传单个文件
     */
    public FileUploadResult uploadFile(MultipartFile file) throws IOException {
        log.info("Starting file upload: {}", file.getOriginalFilename());
        
        // 验证文件
        validateFile(file);
        
        // 存储文件
        FileStorageService.FileStorageResult storageResult = fileStorageService.storeFile(file);
        
        // 处理文件
        FileProcessingService.ProcessedFileResult processedResult = fileProcessingService.processFile(
                file.getInputStream(), 
                storageResult.getFileName(), 
                file.getContentType()
        );
        
        FileUploadResult result = new FileUploadResult();
        result.setFileName(storageResult.getFileName());
        result.setOriginalFileName(file.getOriginalFilename());
        result.setFileUrl(storageResult.getFileUrl());
        result.setStorageType(storageResult.getStorageType());
        result.setFileSize(storageResult.getFileSize());
        result.setContentType(file.getContentType());
        result.setSuccess(true);
        
        log.info("File upload completed successfully: {}", file.getOriginalFilename());
        return result;
    }

    /**
     * 批量上传文件
     */
    public List<FileUploadResult> uploadFiles(List<MultipartFile> files) throws IOException {
        List<FileUploadResult> results = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                FileUploadResult result = uploadFile(file);
                results.add(result);
            } catch (Exception e) {
                log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                
                FileUploadResult errorResult = new FileUploadResult();
                errorResult.setOriginalFileName(file.getOriginalFilename());
                errorResult.setSuccess(false);
                errorResult.setErrorMessage(e.getMessage());
                results.add(errorResult);
            }
        }
        
        return results;
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) throws IOException {
        FrameworkProperties.FileManagement config = frameworkProperties.getFileManagement();
        
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // 检查文件大小
        if (file.getSize() > config.getMaxFileSize()) {
            throw new IllegalArgumentException("File size exceeds limit: " + file.getSize());
        }
        
        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !isAllowedFileType(originalFilename, config.getAllowedExtensions())) {
            throw new IllegalArgumentException("File type not allowed: " + originalFilename);
        }
        
        log.debug("File validation passed: {}", originalFilename);
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
     * 文件上传结果
     */
    public static class FileUploadResult {
        private String fileName;
        private String originalFileName;
        private String fileUrl;
        private String storageType;
        private long fileSize;
        private String contentType;
        private boolean success;
        private String errorMessage;

        // Getters and Setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getOriginalFileName() { return originalFileName; }
        public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
        
        public String getFileUrl() { return fileUrl; }
        public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
        
        public String getStorageType() { return storageType; }
        public void setStorageType(String storageType) { this.storageType = storageType; }
        
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
