package com.atguigu.yygh.gateway.globalcors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/8 18:44
 */
@Configuration
public class CorsConfig {


    //CorsWebFilter 该bean的作用：全局的跨域处理
    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedMethod("*");//所有请求方式  get  post  put ...
        config.addAllowedOrigin("*");//所有的源都支持跨域（无论哪个客户端发起的请求，都支持跨域处理）
        config.addAllowedHeader("*");//允许请求中存在所有类型的请求头   请求：请求头 + 请求体 + 请求行
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);//针对所有请求
        return new CorsWebFilter(source);
    }
}
