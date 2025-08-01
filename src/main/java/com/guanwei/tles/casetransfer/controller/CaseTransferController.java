package com.guanwei.tles.casetransfer.controller;

import com.guanwei.framework.common.controller.BaseController;
import com.guanwei.tles.casetransfer.entity.Case;
import com.guanwei.tles.casetransfer.service.CaseTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/case-transfer")
@RequiredArgsConstructor
@Tag(name = "案件转存管理", description = "案件数据转存相关接口")
public class CaseTransferController extends BaseController<CaseTransferService, Case> {

    @Autowired
    private final CaseTransferService caseTransferService;

    @PostMapping("/sync/{caseId}")
    @Operation(summary = "手动同步案件数据", description = "手动同步指定案件数据到MongoDB")
    public void syncCase(@Parameter(description = "案件ID", required = true) @PathVariable String caseId) {
        log.info("手动同步案件数据: {}", caseId);
        caseTransferService.syncCaseToMongoDB(caseId);
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查服务运行状态")
    public String health() {
        return "案件转存服务运行正常";
    }
}