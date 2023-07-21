package com.atguigu.yygh.user.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/10 15:14
 */
@EnableDiscoveryClient//发布微服务
@EnableFeignClients(basePackages = "com.atguigu")//从该包以及该包的子包下扫描feign接口
@MapperScan(basePackages = "com.atguigu.yygh.user.mapper")//mapper接口所在的包（完整的包名）
@ComponentScan(basePackages = "com.atguigu")//从该包以及该包的子包下扫描bean组件以及config配置类SwaggerConfig
@Configuration
public class UserConfig {

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    //spring-web包下的一个轻量级的web客户端，用于发起http请求


    //解决乱码问题
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        //使用restTemplate发请求，解决中文返回值乱码问题
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

}

