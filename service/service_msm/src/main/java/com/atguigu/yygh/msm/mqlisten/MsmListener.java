package com.atguigu.yygh.msm.mqlisten;

import com.atguigu.yygh.mq.consts.MqConst;
import com.atguigu.yygh.vo.msm.MsmVo;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/19 19:38
 */
@Component
public class MsmListener {


    //这个方法在什么时候被执行？ 队列中有了新的消息
    //方法的参数就是队列中的消息对象
    @RabbitListener(
            bindings = {
                    @QueueBinding(
                            value = @Queue(name = MqConst.QUEUE_MSM_ITEM, durable = "true"),
                            exchange = @Exchange(name = MqConst.EXCHANGE_DIRECT_MSM, durable = "true"),
                            key = {MqConst.ROUTING_MSM_ITEM}
                    )
            }
    )
    public void listenerTwo(MsmVo msmVo) {
        System.out.println("短信服务监听到第二个队列中的消息：" + msmVo);

        String phone = msmVo.getPhone();//就诊人手机号
        Object message = msmVo.getParam().get("message");//消息内容

        //发送短信通知（模拟）
        System.out.println("接收通知的手机号:" + phone);
        System.out.println("短信内容：" + message);

    }
}
