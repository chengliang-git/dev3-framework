//package com.guanwei.framework.common.service.impl;
//
//import com.guanwei.framework.common.entity.BaseMongoEntity;
//import com.guanwei.framework.common.repository.BaseMongoRepository;
//import com.guanwei.framework.common.service.BaseMongoService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
///**
// * MongoDB基础服务实现类
// * 提供通用的MongoDB业务操作实现
// *
// * @param <R> Repository类型
// * @param <T> 实体类型
// * @author Enterprise Framework
// * @since 1.0.0
// */
//public abstract class BaseMongoServiceImpl<R extends BaseMongoRepository<T>, T extends BaseMongoEntity>
//        implements BaseMongoService<T> {
//
//    @Autowired
//    protected R baseMongoRepository;
//
//    @Override
//    public Optional<T> findById(String id) {
//        return baseMongoRepository.findById(id);
//    }
//
//    @Override
//    public List<T> findAll() {
//        return baseMongoRepository.findAll();
//    }
//
//    @Override
//    public Page<T> findPage(Pageable pageable) {
//        return baseMongoRepository.findAll(pageable);
//    }
//
//    @Override
//    public T save(T entity) {
//        return baseMongoRepository.save(entity);
//    }
//
//    @Override
//    public List<T> saveAll(List<T> entities) {
//        return baseMongoRepository.saveAll(entities);
//    }
//
//    @Override
//    public boolean removeById(String id) {
//        baseMongoRepository.deleteById(id);
//        return true;
//    }
//
//    @Override
//    public boolean removeBatchByIds(List<String> ids) {
//        baseMongoRepository.deleteAllById(ids);
//        return true;
//    }
//
//    @Override
//    public List<T> findByDelFlag(Integer delFlag) {
//        return baseMongoRepository.findByDelFlag(delFlag);
//    }
//
//    @Override
//    public Page<T> findByDelFlag(Integer delFlag, Pageable pageable) {
//        return baseMongoRepository.findByDelFlag(delFlag, pageable);
//    }
//
//    @Override
//    public List<T> findByCreator(String creator) {
//        return baseMongoRepository.findByCreator(creator);
//    }
//
//    @Override
//    public List<T> findByModifier(String modifier) {
//        return baseMongoRepository.findByModifier(modifier);
//    }
//
//    @Override
//    public List<T> findAllByOrderByOrderNoAsc() {
//        return baseMongoRepository.findAllByOrderByOrderNoAsc();
//    }
//
//    @Override
//    public List<T> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
//        return baseMongoRepository.findByCreateTimeBetween(startTime, endTime);
//    }
//
//    @Override
//    public List<T> findByModifyTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
//        return baseMongoRepository.findByModifyTimeBetween(startTime, endTime);
//    }
//
//    @Override
//    public boolean logicDelete(String id) {
//        return baseMongoRepository.logicDelete(id);
//    }
//
//    @Override
//    public boolean logicDeleteBatch(List<String> ids) {
//        return baseMongoRepository.logicDeleteBatch(ids);
//    }
//
//    @Override
//    public boolean restore(String id) {
//        return baseMongoRepository.restore(id);
//    }
//
//    @Override
//    public Integer getNextOrderNo() {
//        Optional<T> topEntity = baseMongoRepository.findTopByOrderByOrderNoDesc();
//        if (topEntity.isPresent()) {
//            Integer currentMaxOrderNo = topEntity.get().getOrderNo();
//            return currentMaxOrderNo != null ? currentMaxOrderNo + 1 : 1;
//        }
//        return 1;
//    }
//}