package com.guanwei.framework.common.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guanwei.framework.common.entity.BaseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 基础数据访问层接口
 * 提供通用的CRUD操作和分页查询功能
 *
 * @param <M> Mapper类型
 * @param <T> 实体类型
 * @author Enterprise Framework
 * @since 1.0.0
 */
public abstract class BaseRepository<M extends BaseMapper<T>, T extends BaseEntity>
        extends ServiceImpl<M, T> {

    /**
     * 根据ID查询实体（返回Optional）
     */
    public Optional<T> findById(String id) {
        return Optional.ofNullable(getById(id));
    }

    /**
     * 根据条件查询单个实体
     */
    public Optional<T> findOne(QueryWrapper<T> queryWrapper) {
        return Optional.ofNullable(getOne(queryWrapper));
    }

    /**
     * 根据条件查询实体列表
     */
    public List<T> findList(QueryWrapper<T> queryWrapper) {
        return list(queryWrapper);
    }

    /**
     * 分页查询
     */
    public Page<T> findPage(Page<T> page, QueryWrapper<T> queryWrapper) {
        return page(page, queryWrapper);
    }

    /**
     * 根据条件统计数量
     */
    public long count(QueryWrapper<T> queryWrapper) {
        return count(queryWrapper);
    }

    /**
     * 根据条件判断是否存在
     */
    public boolean exists(QueryWrapper<T> queryWrapper) {
        return count(queryWrapper) > 0;
    }

    /**
     * 保存实体（新增或更新）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdate(T entity) {
        return super.saveOrUpdate(entity);
    }

    /**
     * 批量保存
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatch(List<T> entityList) {
        return super.saveBatch(entityList);
    }

    /**
     * 根据ID删除
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(String id) {
        return super.removeById(id);
    }

    /**
     * 根据条件删除
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(QueryWrapper<T> queryWrapper) {
        return remove(queryWrapper);
    }

    /**
     * 批量删除
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean removeBatchByIds(List<String> ids) {
        return removeByIds(ids);
    }
}