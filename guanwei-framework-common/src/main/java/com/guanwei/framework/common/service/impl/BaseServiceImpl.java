package com.guanwei.framework.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guanwei.framework.common.entity.BaseEntity;
import com.guanwei.framework.common.repository.BaseRepository;
import com.guanwei.framework.common.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * 基础服务层实现类
 * 提供通用的业务操作实现
 *
 * @param <M> Mapper类型
 * @param <T> 实体类型
 * @author Enterprise Framework
 * @since 1.0.0
 */
public abstract class BaseServiceImpl<M extends BaseMapper<T>, T extends BaseEntity>
        implements BaseService<T> {

    @Autowired
    protected BaseRepository<M, T> baseRepository;

    @Override
    public Optional<T> findById(String id) {
        return baseRepository.findById(id);
    }

    @Override
    public Optional<T> findOne(QueryWrapper<T> queryWrapper) {
        return baseRepository.findOne(queryWrapper);
    }

    @Override
    public List<T> findList(QueryWrapper<T> queryWrapper) {
        return baseRepository.findList(queryWrapper);
    }

    @Override
    public List<T> findAll() {
        return baseRepository.list();
    }

    @Override
    public Page<T> findPage(Page<T> page, QueryWrapper<T> queryWrapper) {
        return baseRepository.findPage(page, queryWrapper);
    }

    @Override
    public long count(QueryWrapper<T> queryWrapper) {
        return baseRepository.count(queryWrapper);
    }

    @Override
    public boolean exists(QueryWrapper<T> queryWrapper) {
        return baseRepository.exists(queryWrapper);
    }

    @Override
    public boolean saveOrUpdate(T entity) {
        return baseRepository.saveOrUpdate(entity);
    }

    @Override
    public boolean saveBatch(List<T> entityList) {
        return baseRepository.saveBatch(entityList);
    }

    @Override
    public boolean removeById(String id) {
        return baseRepository.removeById(id);
    }

    @Override
    public boolean remove(QueryWrapper<T> queryWrapper) {
        return baseRepository.remove(queryWrapper);
    }

    @Override
    public boolean removeBatchByIds(List<String> ids) {
        return baseRepository.removeBatchByIds(ids);
    }
}