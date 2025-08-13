package com.guanwei.framework.controller;

import com.guanwei.framework.config.file.FileDownloadService;
import com.guanwei.framework.config.file.FileUploadService;
import com.guanwei.framework.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文件管理控制器
 * 提供文件上传、下载、管理等功能
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "文件管理", description = "文件上传下载管理接口")
public class FileManagementController {

    private final FileUploadService fileUploadService;
    private final FileDownloadService fileDownloadService;

    @Autowired
    public FileManagementController(FileUploadService fileUploadService, 
                                 FileDownloadService fileDownloadService) {
        this.fileUploadService = fileUploadService;
        this.fileDownloadService = fileDownloadService;
    }

    /**
     * 上传单个文件
     */
    @PostMapping("/upload")
    @Operation(summary = "上传单个文件", description = "上传单个文件到存储系统")
    public Result<FileUploadService.FileUploadResult> uploadFile(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file) {
        try {
            FileUploadService.FileUploadResult result = fileUploadService.uploadFile(file);
            return Result.success(result);
        } catch (Exception e) {
            log.error("File upload failed", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传文件
     */
    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传文件", description = "批量上传多个文件到存储系统")
    public Result<List<FileUploadService.FileUploadResult>> uploadFiles(
            @Parameter(description = "文件列表") @RequestParam("files") List<MultipartFile> files) {
        try {
            List<FileUploadService.FileUploadResult> results = fileUploadService.uploadFiles(files);
            return Result.success(results);
        } catch (Exception e) {
            log.error("Batch file upload failed", e);
            return Result.error("批量文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{fileName}")
    @Operation(summary = "下载文件", description = "下载指定文件")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "文件名") @PathVariable String fileName) {
        try {
            return fileDownloadService.downloadFile(fileName);
        } catch (Exception e) {
            log.error("File download failed: {}", fileName, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 预览文件
     */
    @GetMapping("/preview/{fileName}")
    @Operation(summary = "预览文件", description = "预览指定文件")
    public ResponseEntity<Resource> previewFile(
            @Parameter(description = "文件名") @PathVariable String fileName) {
        try {
            return fileDownloadService.previewFile(fileName);
        } catch (Exception e) {
            log.error("File preview failed: {}", fileName, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/info/{fileName}")
    @Operation(summary = "获取文件信息", description = "获取指定文件的详细信息")
    public Result<FileDownloadService.FileInfo> getFileInfo(
            @Parameter(description = "文件名") @PathVariable String fileName) {
        try {
            FileDownloadService.FileInfo fileInfo = fileDownloadService.getFileInfo(fileName);
            return Result.success(fileInfo);
        } catch (Exception e) {
            log.error("Failed to get file info: {}", fileName, e);
            return Result.error("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 生成预签名下载URL（S3）
     */
    @GetMapping("/presigned-url/{fileName}")
    @Operation(summary = "生成预签名下载URL", description = "生成S3预签名下载URL")
    public Result<String> generatePresignedUrl(
            @Parameter(description = "文件名") @PathVariable String fileName) {
        try {
            String presignedUrl = fileDownloadService.generatePresignedDownloadUrl(fileName);
            if (presignedUrl != null) {
                return Result.success(presignedUrl);
            } else {
                return Result.error("预签名URL生成失败");
            }
        } catch (Exception e) {
            log.error("Failed to generate presigned URL: {}", fileName, e);
            return Result.error("生成预签名URL失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查文件管理服务状态")
    public Result<String> health() {
        return Result.success("File management service is running");
    }
}
