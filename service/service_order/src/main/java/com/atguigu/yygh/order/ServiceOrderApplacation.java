package com.atguigu.yygh.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/14 19:47
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.atguigu")
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.atguigu")//为了能扫到service_util下的实体类
public class ServiceOrderApplacation {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOrderApplacation.class, args);
    }
}
