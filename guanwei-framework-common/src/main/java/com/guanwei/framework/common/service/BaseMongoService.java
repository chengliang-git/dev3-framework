package com.guanwei.framework.common.service;

import com.guanwei.framework.common.entity.BaseMongoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB基础服务接口
 * 定义通用的MongoDB业务操作方法
 *
 * @param <T> 实体类型
 * @author Enterprise Framework
 * @since 1.0.0
 */
public interface BaseMongoService<T extends BaseMongoEntity> {

    /**
     * 根据ID查询实体
     */
    Optional<T> findById(String id);

    /**
     * 查询所有实体
     */
    List<T> findAll();

    /**
     * 分页查询
     */
    Page<T> findPage(Pageable pageable);

    /**
     * 保存实体
     */
    T save(T entity);

    /**
     * 批量保存
     */
    List<T> saveAll(List<T> entities);

    /**
     * 根据ID删除
     */
    boolean removeById(String id);

    /**
     * 批量删除
     */
    boolean removeBatchByIds(List<String> ids);

    /**
     * 根据删除标记查询
     */
    List<T> findByDelFlag(Integer delFlag);

    /**
     * 根据删除标记分页查询
     */
    Page<T> findByDelFlag(Integer delFlag, Pageable pageable);

    /**
     * 根据创建人查询
     */
    List<T> findByCreator(String creator);

    /**
     * 根据修改人查询
     */
    List<T> findByModifier(String modifier);

    /**
     * 根据顺序号排序查询
     */
    List<T> findAllByOrderByOrderNoAsc();

    /**
     * 根据创建时间范围查询
     */
    List<T> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据修改时间范围查询
     */
    List<T> findByModifyTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 逻辑删除
     */
    boolean logicDelete(String id);

    /**
     * 批量逻辑删除
     */
    boolean logicDeleteBatch(List<String> ids);

    /**
     * 恢复删除
     */
    boolean restore(String id);

    /**
     * 获取下一个顺序号
     */
    Integer getNextOrderNo();
}