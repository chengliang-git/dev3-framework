package com.guanwei.framework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guanwei.framework.common.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

} 