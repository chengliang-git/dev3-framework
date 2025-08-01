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

    // 子类需要自己注入具体的Service
    // @Autowired
    // protected S baseService;

    // 子类需要自己实现具体的CRUD方法
    // 或者重写这些方法，注入具体的Service
}