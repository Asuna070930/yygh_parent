package com.atguigu.yygh.order.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/6/25 18:54
 */
@Configuration //当前是一个配置类 可以配置各种 bean 组件
@EnableTransactionManagement //开启事务
@MapperScan(basePackages = "com.atguigu.yygh.order.mapper")
public class OrderConfig {

    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }
}
