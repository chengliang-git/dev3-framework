package com.guanwei.framework.common.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guanwei.framework.common.entity.BaseEntity;

import java.util.List;
import java.util.Optional;

/**
 * 基础服务层接口
 * 定义通用的业务操作方法
 *
 * @param <T> 实体类型
 * @author Enterprise Framework
 * @since 1.0.0
 */
public interface BaseService<T extends BaseEntity> {

    /**
     * 根据ID查询实体
     */
    Optional<T> findById(String id);

    /**
     * 根据条件查询单个实体
     */
    Optional<T> findOne(QueryWrapper<T> queryWrapper);

    /**
     * 根据条件查询实体列表
     */
    List<T> findList(QueryWrapper<T> queryWrapper);

    /**
     * 查询所有实体
     */
    List<T> findAll();

    /**
     * 分页查询
     */
    Page<T> findPage(Page<T> page, QueryWrapper<T> queryWrapper);

    /**
     * 根据条件统计数量
     */
    long count(QueryWrapper<T> queryWrapper);

    /**
     * 根据条件判断是否存在
     */
    boolean exists(QueryWrapper<T> queryWrapper);

    /**
     * 保存实体（新增或更新）
     */
    boolean saveOrUpdate(T entity);

    /**
     * 批量保存
     */
    boolean saveBatch(List<T> entityList);

    /**
     * 根据ID删除
     */
    boolean removeById(String id);

    /**
     * 根据条件删除
     */
    boolean remove(QueryWrapper<T> queryWrapper);

    /**
     * 批量删除
     */
    boolean removeBatchByIds(List<String> ids);
}