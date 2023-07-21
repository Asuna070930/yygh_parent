package com.atguigu.yygh.mq.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/15 14:06
 */
@Service
public class RabbitService {

    //直接注入使用
    //springboot自动配置，完成了该对模板对象的配置
    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     *
     * @param exchangeName 交换机名称
     * @param routingKey 路由键名称
     * @param message 消息对象
     */
    public void sendMessage(String exchangeName,String routingKey,Object message){
        rabbitTemplate.convertAndSend(exchangeName,routingKey,message);
    }

}