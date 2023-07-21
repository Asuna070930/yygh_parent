package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OrderInfoService extends IService<OrderInfo> {
    Long submitOrder(Long patientId, String scheduleId);

    OrderInfo getOrder(Long orderId);
}
