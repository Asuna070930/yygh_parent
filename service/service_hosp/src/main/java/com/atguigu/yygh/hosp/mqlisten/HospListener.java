package com.atguigu.yygh.hosp.mqlisten;

import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.mq.consts.MqConst;
import com.atguigu.yygh.mq.service.RabbitService;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/19 19:38
 */
@Component
public class HospListener {


    @Autowired
    ScheduleService scheduleService;

    @Autowired
    RabbitService rabbitService;

    //这个方法在什么时候被执行？ 队列中有了新的消息
    //方法的参数就是队列中的消息对象
    @RabbitListener(
            bindings = {
                    @QueueBinding(
                            //队列和交换机不存在时，会自动创建
                            value = @Queue(name = MqConst.QUEUE_ORDER, durable = "true"), //当前监听程序负责监听的队列的名称
                            exchange = @Exchange(name = MqConst.EXCHANGE_DIRECT_ORDER, durable = "true"),//当前队列绑定到的交换机，默认是direct类型
                            key = {MqConst.ROUTING_ORDER} //队列和交换机绑定时指定的路由键
                    )
            }
    )
    public void listenerOne(OrderMqVo orderMqVo) {
        System.out.println("医院服务的监听程序，监听第一个队列，消息内容：" + orderMqVo);

        //1、更新两个num
        scheduleService.updateSchedule(orderMqVo);

        //2、向第二个队列中发送消息
        MsmVo msmVo = orderMqVo.getMsmVo();
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);

    }
}
