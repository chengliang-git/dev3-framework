package com.guanwei.framework.common.controller;

import com.guanwei.framework.common.entity.BaseMongoEntity;
import com.guanwei.framework.common.result.Result;
import com.guanwei.framework.common.service.BaseMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB基础控制器类
 * 提供通用的MongoDB REST API操作
 *
 * @param <S> 服务类型
 * @param <T> 实体类型
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Tag(name = "MongoDB基础操作", description = "通用的MongoDB CRUD操作接口")
public abstract class BaseMongoController<S extends BaseMongoService<T>, T extends BaseMongoEntity> {

    @Autowired
    protected S baseMongoService;

    @Operation(summary = "根据ID查询", description = "根据主键ID查询实体")
    @GetMapping("/{id}")
    public Result<T> getById(@Parameter(description = "主键ID") @PathVariable String id) {
        return baseMongoService.findById(id)
                .map(Result::success)
                .orElse(Result.error("数据不存在"));
    }

    @Operation(summary = "查询所有", description = "查询所有实体列表")
    @GetMapping("/list")
    public Result<List<T>> list() {
        List<T> list = baseMongoService.findAll();
        return Result.success(list);
    }

    @Operation(summary = "分页查询", description = "分页查询实体列表")
    @GetMapping("/page")
    public Result<Page<T>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<T> result = baseMongoService.findPage(pageable);
        return Result.success(result);
    }

    @Operation(summary = "新增", description = "新增实体")
    @PostMapping
    public Result<T> save(@RequestBody T entity) {
        T savedEntity = baseMongoService.save(entity);
        return Result.success(savedEntity);
    }

    @Operation(summary = "更新", description = "更新实体")
    @PutMapping("/{id}")
    public Result<T> update(
            @Parameter(description = "主键ID") @PathVariable String id,
            @RequestBody T entity) {
        entity.setId(id);
        T updatedEntity = baseMongoService.save(entity);
        return Result.success(updatedEntity);
    }

    @Operation(summary = "删除", description = "根据ID删除实体")
    @DeleteMapping("/{id}")
    public Result<Boolean> removeById(@Parameter(description = "主键ID") @PathVariable String id) {
        boolean success = baseMongoService.removeById(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @Operation(summary = "批量删除", description = "根据ID列表批量删除")
    @DeleteMapping("/batch")
    public Result<Boolean> removeBatch(@RequestBody List<String> ids) {
        boolean success = baseMongoService.removeBatchByIds(ids);
        return success ? Result.success(true) : Result.error("批量删除失败");
    }

    @Operation(summary = "逻辑删除", description = "根据ID逻辑删除实体")
    @PutMapping("/{id}/logic-delete")
    public Result<Boolean> logicDelete(@Parameter(description = "主键ID") @PathVariable String id) {
        boolean success = baseMongoService.logicDelete(id);
        return success ? Result.success(true) : Result.error("逻辑删除失败");
    }

    @Operation(summary = "恢复删除", description = "根据ID恢复已删除的实体")
    @PutMapping("/{id}/restore")
    public Result<Boolean> restore(@Parameter(description = "主键ID") @PathVariable String id) {
        boolean success = baseMongoService.restore(id);
        return success ? Result.success(true) : Result.error("恢复失败");
    }

    @Operation(summary = "根据删除标记查询", description = "根据删除标记查询实体列表")
    @GetMapping("/del-flag/{delFlag}")
    public Result<List<T>> findByDelFlag(@Parameter(description = "删除标记") @PathVariable Integer delFlag) {
        List<T> list = baseMongoService.findByDelFlag(delFlag);
        return Result.success(list);
    }

    @Operation(summary = "根据创建人查询", description = "根据创建人查询实体列表")
    @GetMapping("/creator/{creator}")
    public Result<List<T>> findByCreator(@Parameter(description = "创建人") @PathVariable String creator) {
        List<T> list = baseMongoService.findByCreator(creator);
        return Result.success(list);
    }

    @Operation(summary = "根据修改人查询", description = "根据修改人查询实体列表")
    @GetMapping("/modifier/{modifier}")
    public Result<List<T>> findByModifier(@Parameter(description = "修改人") @PathVariable String modifier) {
        List<T> list = baseMongoService.findByModifier(modifier);
        return Result.success(list);
    }

    @Operation(summary = "按顺序号排序查询", description = "按顺序号升序查询实体列表")
    @GetMapping("/order-by")
    public Result<List<T>> findAllByOrderByOrderNoAsc() {
        List<T> list = baseMongoService.findAllByOrderByOrderNoAsc();
        return Result.success(list);
    }

    @Operation(summary = "根据创建时间范围查询", description = "根据创建时间范围查询实体列表")
    @GetMapping("/create-time-range")
    public Result<List<T>> findByCreateTimeBetween(
            @Parameter(description = "开始时间") @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam LocalDateTime endTime) {
        List<T> list = baseMongoService.findByCreateTimeBetween(startTime, endTime);
        return Result.success(list);
    }

    @Operation(summary = "根据修改时间范围查询", description = "根据修改时间范围查询实体列表")
    @GetMapping("/modify-time-range")
    public Result<List<T>> findByModifyTimeBetween(
            @Parameter(description = "开始时间") @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam LocalDateTime endTime) {
        List<T> list = baseMongoService.findByModifyTimeBetween(startTime, endTime);
        return Result.success(list);
    }

    @Operation(summary = "获取下一个顺序号", description = "获取下一个可用的顺序号")
    @GetMapping("/next-order-no")
    public Result<Integer> getNextOrderNo() {
        Integer nextOrderNo = baseMongoService.getNextOrderNo();
        return Result.success(nextOrderNo);
    }
}