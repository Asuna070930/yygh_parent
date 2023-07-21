package com.atguigu.cmn;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/6/25 18:25
 */

@ComponentScan(basePackages = "com.atguigu") //为了能扫描到service-util下的配置类(或组件)
@SpringBootApplication
@MapperScan(basePackages = "com.atguigu.yygh.cmn.mapper")
public class ServiceCmnApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceCmnApplication.class, args);
    }
}
