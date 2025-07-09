package com.enterprise.framework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.framework.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
} 