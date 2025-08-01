package com.guanwei.framework.common.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guanwei.framework.common.entity.BaseEntity;
import com.guanwei.framework.common.result.Result;
import com.guanwei.framework.common.service.BaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 基础控制器类
 * 提供通用的REST API操作
 *
 * @param <S> 服务类型
 * @param <T> 实体类型
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Tag(name = "基础操作", description = "通用的CRUD操作接口")
public abstract class BaseController<S extends BaseService<T>, T extends BaseEntity> {

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
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") long size) {
        Page<T> page = new Page<>(current, size);
        Page<T> result = getService().findPage(page, new QueryWrapper<>());
        return Result.success(result);
    }

    @Operation(summary = "新增", description = "新增实体")
    @PostMapping
    public Result<Boolean> save(@RequestBody T entity) {
        boolean success = getService().saveOrUpdate(entity);
        return success ? Result.success(true) : Result.error("新增失败");
    }

    @Operation(summary = "更新", description = "更新实体")
    @PutMapping("/{id}")
    public Result<Boolean> update(
            @Parameter(description = "主键ID") @PathVariable String id,
            @RequestBody T entity) {
        entity.setId(id);
        boolean success = getService().saveOrUpdate(entity);
        return success ? Result.success(true) : Result.error("更新失败");
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
        long count = getService().count(new QueryWrapper<>());
        return Result.success(count);
    }
}