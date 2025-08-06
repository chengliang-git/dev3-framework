//package com.guanwei.framework.common.repository;
//
//import com.guanwei.framework.common.entity.BaseMongoEntity;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.data.mongodb.repository.Query;
//import org.springframework.data.repository.NoRepositoryBean;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
///**
// * MongoDB基础Repository接口
// * 提供通用的MongoDB操作
// *
// * @param <T> 实体类型
// * @author Enterprise Framework
// * @since 1.0.0
// */
//@NoRepositoryBean
//public interface BaseMongoRepository<T extends BaseMongoEntity> extends MongoRepository<T, String> {
//
//    /**
//     * 根据ID查询实体（返回Optional）
//     */
//    default Optional<T> findById(String id) {
//        return findById(id);
//    }
//
//    /**
//     * 根据删除标记查询
//     */
//    @Query("{'delFlag': ?0}")
//    List<T> findByDelFlag(Integer delFlag);
//
//    /**
//     * 根据删除标记分页查询
//     */
//    @Query("{'delFlag': ?0}")
//    Page<T> findByDelFlag(Integer delFlag, Pageable pageable);
//
//    /**
//     * 根据创建人查询
//     */
//    @Query("{'creator': ?0, 'delFlag': 0}")
//    List<T> findByCreator(String creator);
//
//    /**
//     * 根据修改人查询
//     */
//    @Query("{'modifier': ?0, 'delFlag': 0}")
//    List<T> findByModifier(String modifier);
//
//    /**
//     * 根据顺序号排序查询
//     */
//    @Query("{'delFlag': 0}")
//    List<T> findAllByOrderByOrderNoAsc();
//
//    /**
//     * 根据创建时间范围查询
//     */
//    @Query("{'createTime': {$gte: ?0, $lte: ?1}, 'delFlag': 0}")
//    List<T> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
//
//    /**
//     * 根据修改时间范围查询
//     */
//    @Query("{'modifyTime': {$gte: ?0, $lte: ?1}, 'delFlag': 0}")
//    List<T> findByModifyTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
//
//    /**
//     * 逻辑删除
//     */
//    default boolean logicDelete(String id) {
//        Optional<T> entity = findById(id);
//        if (entity.isPresent()) {
//            T t = entity.get();
//            t.setDelFlag(1);
//            save(t);
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * 批量逻辑删除
//     */
//    default boolean logicDeleteBatch(List<String> ids) {
//        List<T> entities = findAllById(ids);
//        entities.forEach(entity -> entity.setDelFlag(1));
//        saveAll(entities);
//        return true;
//    }
//
//    /**
//     * 恢复删除
//     */
//    default boolean restore(String id) {
//        Optional<T> entity = findById(id);
//        if (entity.isPresent()) {
//            T t = entity.get();
//            t.setDelFlag(0);
//            save(t);
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * 获取最大顺序号
//     */
//    @Query(value = "{'delFlag': 0}", sort = "{'orderNo': -1}")
//    Optional<T> findTopByOrderByOrderNoDesc();
//}