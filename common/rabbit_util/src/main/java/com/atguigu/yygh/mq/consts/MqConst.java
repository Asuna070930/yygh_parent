package com.atguigu.yygh.mq.consts;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/15 11:57
 */
public class MqConst {

    //队列名称+交换机名+key


    /**
     * 订单服务（队列信息）---第一个队列
     */
    public static final String EXCHANGE_DIRECT_ORDER = "exchange.direct.order";//第一个交换机名称
    public static final String ROUTING_ORDER = "order";//队列和交换机绑定时指定的key
    public static final String QUEUE_ORDER  = "queue.order"; //第一个队列名称

    /**
     * 短信服务（队列信息）
     */
    public static final String EXCHANGE_DIRECT_MSM = "exchange.direct.msm";//第二个交换机名称
    public static final String ROUTING_MSM_ITEM = "msm.item";//队列和交换机绑定时指定的key
    public static final String QUEUE_MSM_ITEM  = "queue.msm.item";//第二个队列名称

}
