package com.atguigu.yygh.mq.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/15 14:32
 */
@Configuration
public class MQConfig {

    //消息转换器
    //目的：可以自定义vo作为消息对象进行传递
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}