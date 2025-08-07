package com.guanwei.tles.casetransfer.controller;

import com.guanwei.framework.common.controller.BaseController;
import com.guanwei.framework.common.result.Result;
import com.guanwei.tles.casetransfer.entity.oracle.CaseInfoEntity;
import com.guanwei.tles.casetransfer.service.CaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 案件管理控制器
 * 继承基础框架，提供完整的案件CRUD操作
 * 
 * @author Guanwei Framework
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
@Tag(name = "案件管理", description = "案件数据管理相关接口")
public class CaseController extends BaseController<CaseService, CaseInfoEntity> {

    private final CaseService caseService;

    protected CaseService getService() {
        return caseService;
    }

    // ==================== 基础CRUD操作 ====================
    // 继承自BaseController，自动提供以下接口：
    // GET /api/cases/{id} - 根据ID查询
    // GET /api/cases/list - 查询所有
    // GET /api/cases/page - 分页查询
    // POST /api/cases - 新增
    // PUT /api/cases/{id} - 更新
    // DELETE /api/cases/{id} - 删除
    // DELETE /api/cases/batch - 批量删除
    // GET /api/cases/count - 统计数量

    // ==================== 案件特有业务操作 ====================

    @GetMapping("/caseId/{caseId}")
    @Operation(summary = "根据案件ID查询", description = "根据案件ID查询案件详细信息")
    public Result<CaseInfoEntity> getByCaseId(
            @Parameter(description = "案件ID", required = true) @PathVariable String caseId) {
        log.info("根据案件ID查询案件信息: {}", caseId);
        CaseInfoEntity caseEntity = caseService.findByCaseId(caseId);
        if (caseEntity != null) {
            return Result.success(caseEntity);
        } else {
            return Result.error("案件不存在");
        }
    }

    @GetMapping("/caseNo/{caseNo}")
    @Operation(summary = "根据案件编号查询", description = "根据案件编号查询案件信息")
    public Result<CaseInfoEntity> getByCaseNo(
            @Parameter(description = "案件编号", required = true) @PathVariable String caseNo) {
        log.info("根据案件编号查询案件信息: {}", caseNo);
        CaseInfoEntity caseEntity = caseService.findByCaseNo(caseNo);
        if (caseEntity != null) {
            return Result.success(caseEntity);
        } else {
            return Result.error("案件不存在");
        }
    }

    @GetMapping("/party/{partyName}")
    @Operation(summary = "根据当事人姓名查询", description = "根据当事人姓名查询案件列表")
    public Result<List<CaseInfoEntity>> getByPartyName(
            @Parameter(description = "当事人姓名", required = true) @PathVariable String partyName) {
        log.info("根据当事人姓名查询案件列表: {}", partyName);
        List<CaseInfoEntity> cases = caseService.findByPartyName(partyName);
        return Result.success(cases);
    }

    @GetMapping("/company/{companyName}")
    @Operation(summary = "根据当事单位名称查询", description = "根据当事单位名称查询案件列表")
    public Result<List<CaseInfoEntity>> getByCompanyName(
            @Parameter(description = "当事单位名称", required = true) @PathVariable String companyName) {
        log.info("根据当事单位名称查询案件列表: {}", companyName);
        List<CaseInfoEntity> cases = caseService.findByCompanyName(companyName);
        return Result.success(cases);
    }

    @GetMapping("/state/{state}")
    @Operation(summary = "根据案件状态查询", description = "根据案件状态查询案件列表")
    public Result<List<CaseInfoEntity>> getByState(
            @Parameter(description = "案件状态", required = true) @PathVariable Integer state) {
        log.info("根据案件状态查询案件列表: {}", state);
        List<CaseInfoEntity> cases = caseService.findByState(state);
        return Result.success(cases);
    }

    @GetMapping("/location/{illegalLocation}")
    @Operation(summary = "根据违法地点查询", description = "根据违法地点查询案件列表")
    public Result<List<CaseInfoEntity>> getByIllegalLocation(
            @Parameter(description = "违法地点", required = true) @PathVariable String illegalLocation) {
        log.info("根据违法地点查询案件列表: {}", illegalLocation);
        List<CaseInfoEntity> cases = caseService.findByIllegalLocation(illegalLocation);
        return Result.success(cases);
    }

    @GetMapping("/time-range")
    @Operation(summary = "根据立案时间范围查询", description = "根据立案时间范围查询案件列表")
    public Result<List<CaseInfoEntity>> getByTimeRange(
            @Parameter(description = "开始时间") @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam LocalDateTime endTime) {
        log.info("根据立案时间范围查询案件列表: {} - {}", startTime, endTime);
        List<CaseInfoEntity> cases = caseService.findByCaseFilingTimeBetween(startTime, endTime);
        return Result.success(cases);
    }

    @GetMapping("/stats")
    @Operation(summary = "统计信息", description = "获取案件统计信息")
    public Result<Object> getStats() {
        log.info("获取案件统计信息");
        // 这里可以添加更详细的统计信息
        return Result.success("案件统计信息（功能待完善）");
    }
}
