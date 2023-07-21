package com.atguigu.yygh.oss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.atguigu")
@ComponentScan(basePackages = "com.atguigu")

//排除数据源的自动配置
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ServiceOSSApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOSSApplication.class,args);
    }
}
