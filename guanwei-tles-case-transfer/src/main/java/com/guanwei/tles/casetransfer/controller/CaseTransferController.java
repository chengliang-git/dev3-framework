package com.guanwei.tles.casetransfer.controller;

import com.guanwei.framework.common.result.Result;
import com.guanwei.tles.casetransfer.service.CaseTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 案件转存控制器 - 专门用于案件转存相关操作
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/case-transfer")
@RequiredArgsConstructor
@Tag(name = "案件转存管理", description = "案件数据转存相关接口")
public class CaseTransferController {

    private final CaseTransferService caseTransferService;

    @PostMapping("/sync/{caseId}")
    @Operation(summary = "手动同步案件数据", description = "手动同步指定案件数据到MongoDB（暂时禁用）")
    public Result<Void> syncCase(
            @Parameter(description = "案件ID", required = true) @PathVariable String caseId) {
        log.info("手动同步案件数据: {} (MongoDB功能暂时禁用)", caseId);
        caseTransferService.syncCaseToMongoDB(caseId);
        return Result.success();
    }

    @PostMapping("/sync/batch")
    @Operation(summary = "批量同步案件数据", description = "批量同步案件数据到MongoDB（暂时禁用）")
    public Result<Void> syncBatchCases(
            @Parameter(description = "案件ID列表", required = true) @RequestBody List<String> caseIds) {
        log.info("批量同步案件数据: {} (MongoDB功能暂时禁用)", caseIds);
        for (String caseId : caseIds) {
            caseTransferService.syncCaseToMongoDB(caseId);
        }
        return Result.success();
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查服务运行状态")
    public Result<String> health() {
        return Result.success("案件转存服务运行正常（MongoDB功能暂时禁用）");
    }

    @GetMapping("/status")
    @Operation(summary = "转存状态", description = "获取案件转存服务状态")
    public Result<Object> getStatus() {
        log.info("获取案件转存服务状态");
        return Result.success("案件转存服务状态（功能待完善）");
    }
}