package com.atguigu.yygh.hosp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/6/25 18:25
 */

@EnableDiscoveryClient
//不写basePackages，当前项目默认从启动类所在包以及子包下开始加载feign接口
@EnableFeignClients(basePackages = "com.atguigu")//开启feign调用，并且指定从哪个基本包下开始加载feign接口
@ComponentScan(basePackages = "com.atguigu") //为了能够扫描到service_util下的配置类（组件）
@SpringBootApplication
public class ServiceHospApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceHospApplication.class, args);
    }
}
