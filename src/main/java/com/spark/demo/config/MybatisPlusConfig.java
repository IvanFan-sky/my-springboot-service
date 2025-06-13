package com.spark.demo.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Date;

/**
 * Mybatis-Plus配置类
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@Configuration
@MapperScan("com.spark.demo.mapper")
@EnableTransactionManagement
public class MybatisPlusConfig implements MetaObjectHandler {

    /**
     * MybatisPlusInterceptor 插件，内含分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 添加分页插件，并指定数据库类型为MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        
        return interceptor;
    }

    /**
     * 插入时字段自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");
        
        // 创建时间自动填充
        this.strictInsertFill(metaObject, "createdTime", Date.class, new Date());
        
        // 更新时间自动填充
        this.strictInsertFill(metaObject, "updatedTime", Date.class, new Date());
        
        // 逻辑删除字段在插入时设置为null（未删除状态）
        if (metaObject.hasSetter("deletedTime")) {
            this.setFieldValByName("deletedTime", null, metaObject);
            log.debug("设置deletedTime为null（未删除状态）");
        }
    }

    /**
     * 更新时字段自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");
        
        // 更新时间自动填充
        this.strictUpdateFill(metaObject, "updatedTime", Date.class, new Date());
        
        // 逻辑删除时填充删除时间
        if (metaObject.hasSetter("deletedTime")) {
            // 检查是否是逻辑删除操作（通过操作类型判断）
            String className = metaObject.getOriginalObject().getClass().getName();
            if (className.contains("LogicDelete") || this.getFieldValByName("deletedTime", metaObject) != null) {
                this.strictUpdateFill(metaObject, "deletedTime", Date.class, new Date());
                log.debug("填充deletedTime为当前时间（逻辑删除）");
            }
        }
    }
}