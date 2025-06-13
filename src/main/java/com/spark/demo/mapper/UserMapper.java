package com.spark.demo.mapper;

/**
 * @author spark
 * @date 2024-07-27
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spark.demo.entity.User;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);
    
    /**
     * 根据ID查询用户（忽略逻辑删除，用于测试）
     * 直接使用SQL查询，绕过MyBatis-Plus的逻辑删除过滤
     */
    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    User selectByIdIgnoreLogicDelete(@Param("id") Long id);
}