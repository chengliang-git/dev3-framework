package com.guanwei.framework.common.controller;

import com.guanwei.framework.common.entity.BaseMongoEntity;
import com.guanwei.framework.common.result.Result;
import com.guanwei.framework.common.service.BaseMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 获取服务实例，子类需要重写此方法
     */
    protected abstract S getService();

    @Operation(summary = "根据ID查询", description = "根据主键ID查询实体")
    @GetMapping("/{id}")
    public Result<T> getById(@Parameter(description = "主键ID") @PathVariable String id) {
        return getService().findById(id)
                .map(Result::success)
                .orElse(Result.error("数据不存在"));
    }

    @Operation(summary = "查询所有", description = "查询所有实体列表")
    @GetMapping("/list")
    public Result<List<T>> list() {
        List<T> list = getService().findAll();
        return Result.success(list);
    }

    @Operation(summary = "分页查询", description = "分页查询实体列表")
    @GetMapping("/page")
    public Result<Page<T>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<T> result = getService().findPage(pageRequest);
        return Result.success(result);
    }

    @Operation(summary = "新增", description = "新增实体")
    @PostMapping
    public Result<T> save(@RequestBody T entity) {
        T saved = getService().save(entity);
        return Result.success(saved);
    }

    @Operation(summary = "更新", description = "更新实体")
    @PutMapping("/{id}")
    public Result<T> update(
            @Parameter(description = "主键ID") @PathVariable String id,
            @RequestBody T entity) {
        entity.setId(id);
        T updated = getService().save(entity);
        return Result.success(updated);
    }

    @Operation(summary = "删除", description = "根据ID删除实体")
    @DeleteMapping("/{id}")
    public Result<Boolean> removeById(@Parameter(description = "主键ID") @PathVariable String id) {
        boolean success = getService().removeById(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @Operation(summary = "批量删除", description = "根据ID列表批量删除")
    @DeleteMapping("/batch")
    public Result<Boolean> removeBatch(@RequestBody List<String> ids) {
        boolean success = getService().removeBatchByIds(ids);
        return success ? Result.success(true) : Result.error("批量删除失败");
    }

    @Operation(summary = "统计数量", description = "统计实体总数")
    @GetMapping("/count")
    public Result<Long> count() {
        List<T> all = getService().findAll();
        return Result.success((long) all.size());
    }
}