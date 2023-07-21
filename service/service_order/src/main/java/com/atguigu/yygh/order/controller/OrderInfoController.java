package com.atguigu.yygh.order.controller;

import com.atguigu.common.result.R;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.order.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/14 20:50
 */
@RequestMapping("/api/order")
@RestController
public class OrderInfoController {

    @Autowired
    OrderInfoService orderInfoService;


    /**
     * 提交订单接口
     *
     *    1、远程服务调用
     *      （1）调用用户服务根据就诊人id获取就诊人
     *      （2）调用医院服务，根据排班id查询到ScheduleOrderVo，用于调用医院端接口时传递的参数  和  创建平台端订单时
     *      （3）调用医院服务，根据hoscode查询该医院的医院设置中的apiUrl，医院端的接口地址 http://医院端系统的ip:port
     *    2、远程调用医院端的“预约下单”接口
     *        该接口的作用？ 在医院端数据库，创建订单；在医院端数据库中，修改排班的可用号源数量，也就是availableNum减一；
     *        调用医院端接口时需要：选择挂号的mongodb中的排班中hoscode+depcode+workDate+amount 这四个字段必须和医院端数据库中对应的排班的字段一致
     *        医院接口返回值code=200表示调用成功，如果不等于200，调用失败，失败的原因大概率就是参数值不匹配；
     *        医院端接口调用成功后，data中返回一些业务数据：医院端创建的订单id + 两个最新的num + 取号时间+取号地点+预约序号
     *
     *
     *    3、利用scheduleOrderVo + patient 创建平台端的订单，存储在`yygh_order`.`order_info` 表中；
     *       提交订单接口最后return返回的是平台端创建的订单id
     *
     *    4、为什么消息队列异步更新mongdob中号源num +  给就这人发送短信通知？
     *       在提交订单接口中，将非核心业务提取出去，异步执行，降低提交订单接口整体的响应时间，提高单位时间内可以处理的并发请求数
     *       具体做法：
     *           （1）封装了rabbit_util, 添加了amqp依赖 --- AMQP是消息协议，rabbitmq基于该协议
     *                                  封装常量类----队列名称+交换机名称+路由键；
     *                                  配置类---MessageConverter消息转换器，目的是为了能够使用自定义的vo对象作为消息进行传递
     *                                  封装了RabbitService，自定义了发送消息的方法，该方法调用了rabbitTemplate中的方法实现的
     *            （2）哪些微服务需要添加rabbit_util的依赖？ 并且application.properties 中添加4个参数（ip+port+name+password）
     *                  service_order  service_hosp  service_msm
     *
     *        异步处理的流程？
     *
     *              订单服务发送消息 ----> 队列1  ----> 医院服务监听 (1) 取出消息对象中的两个num和排班id，更新到mongodb中 （2） 取出msmVo，发送到第二个队列中
     *              第二个队列由短信服务service_msm负责监听---->从msmVo中提取手机号+message短信内容，模拟给指定的手机号发送短信通知。
     *
     *       启动service_hosp 和 service_msm 两个服务之后，在mq的管理控制台才可以看到队列和交换机
     *       监听程序的写法（注解版）：
     *        @RabbitListener(
     *             bindings = {
     *                     @QueueBinding(
     *                             //队列和交换机不存在时，会自动创建
     *                             value = @Queue(name = MqConst.QUEUE_ORDER,durable = "true"), //当前监听程序负责监听的队列的名称
     *                             exchange = @Exchange(name = MqConst.EXCHANGE_DIRECT_ORDER,durable = "true"),//当前队列绑定到的交换机，默认是direct类型
     *                             key = {MqConst.ROUTING_ORDER} //队列和交换机绑定时指定的路由键
     *                     )
     *             }
     *     )
     *
     *
     *
     *
     * @param patientId 就诊人id
     * @param scheduleId mongodb中排班id
     * @return
     */
    @GetMapping("/submitOrder/{patientId}/{scheduleId}")
    public R submitOrder(@PathVariable Long patientId, @PathVariable String scheduleId){
        Long orderId = orderInfoService.submitOrder(patientId,scheduleId);
        return R.ok().data("orderId",orderId);//平台端创建的订单的id
    }

    @GetMapping("getOrder/{orderId}")
    public R getOrder(@PathVariable Long orderId){
        OrderInfo orderInfo = orderInfoService.getOrder(orderId);
        return R.ok().data("orderInfo",orderInfo);
    }

}
