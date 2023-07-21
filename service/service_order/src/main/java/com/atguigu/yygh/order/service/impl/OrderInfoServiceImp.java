package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.exphandler.YyghException;
import com.atguigu.yygh.client.HospFeignClient;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.mq.consts.MqConst;
import com.atguigu.yygh.mq.service.RabbitService;
import com.atguigu.yygh.order.mapper.OrderInfoMapper;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.order.util.HttpRequestHelper;
import com.atguigu.yygh.user.feign.PatientFeignClient;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/14 20:41
 */
@Service
public class OrderInfoServiceImp extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    RabbitService rabbitService;

    @Autowired
    PatientFeignClient patientFeignClient;

    @Autowired
    HospFeignClient hospFeignClient;

    @Override
    public Long submitOrder(Long patientId, String scheduleId) {


        //0、准备数据，调用医院端接口时需要使用
        Patient patient = patientFeignClient.getPatient(patientId);
        ScheduleOrderVo scheduleOrderVo = hospFeignClient.getSchedule(scheduleId);
        String apiUrl = hospFeignClient.getApiUrl(scheduleOrderVo.getHoscode());

        //1、开始调用医院端接口（注意：医院端系统必须启动！！！）
        //（1）拼接医院端接口的路径
        //医院端“预约下单”接口的路径 （/order/submitOrder  固定的，前端的ip，port，协议 需要从医院设置表中去查询）
        //String url = scheduleOrderVo.getApiUrl() +  "/order/submitOrder";
        String url = apiUrl +  "/order/submitOrder";

        //（2）封装医院端接口所需要的参数
        Map<String,Object> map = new HashMap<>();
        map.put("hoscode",scheduleOrderVo.getHoscode());
        map.put("depcode",scheduleOrderVo.getDepcode());
        map.put("hosScheduleId",scheduleOrderVo.getHosScheduleId());
        map.put("reserveDate",scheduleOrderVo.getReserveDate());
        map.put("reserveTime",scheduleOrderVo.getReserveTime());
        map.put("amount",scheduleOrderVo.getAmount());

        map.put("name",patient.getName());
        map.put("sex",patient.getSex());
        map.put("birthdate",patient.getBirthdate());
        map.put("phone",patient.getPhone());
        map.put("isMarry",patient.getIsMarry());
        map.put("provinceCode",patient.getProvinceCode());
        map.put("cityCode",patient.getCityCode());
        map.put("districtCode",patient.getDistrictCode());
        map.put("address",patient.getAddress());
        map.put("contactsName",patient.getContactsName());
        map.put("contactsCertificatesType",patient.getCertificatesType());
        map.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        map.put("contactsPhone",patient.getContactsPhone());
        map.put("isInsure",patient.getIsInsure());

        //（3）使用工具类开始调用医院端接口;  注意：一定启动医院端系统
        JSONObject jsonObject = HttpRequestHelper.sendRequest(map, url);// code + message + data

        //（4）判断医院端接口是否调用成功
        Integer code = jsonObject.getInteger("code");
        if(code!=200){
            throw new YyghException(20001,"医院端接口调用失败");
        }

        //（5）解析医院端的返回值，也就是data中的业务数据
        JSONObject data = jsonObject.getJSONObject("data");

        Long hosRecordId = data.getLong("hosRecordId");//医院端创建订单的主键
        Integer number = data.getInteger("number");//预约序号
        Integer reservedNumber = data.getInteger("reservedNumber");//医院端返回的该排班最新的总号源数量
        Integer availableNumber = data.getInteger("availableNumber");//医院端返回的该排班最新的剩余可用的号源数量
        String fetchTime = data.getString("fetchTime");//取号时间
        String fetchAddress = data.getString("fetchAddress");//取号地点



        //2、创建平台端的订单
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(patient.getUserId());//当前订单对应用户id
        long outTradeNo = System.currentTimeMillis() + new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo+"");//订单编号，保证唯一
        orderInfo.setHoscode(scheduleOrderVo.getHoscode());
        orderInfo.setHosname(scheduleOrderVo.getHosname());
        orderInfo.setDepcode(scheduleOrderVo.getDepcode());
        orderInfo.setDepname(scheduleOrderVo.getDepname());
        orderInfo.setTitle(scheduleOrderVo.getTitle());
        orderInfo.setScheduleId(scheduleId);//mongodb中排班id
        orderInfo.setReserveDate(scheduleOrderVo.getReserveDate());
        orderInfo.setReserveTime(scheduleOrderVo.getReserveTime());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setHosRecordId(hosRecordId+"");//医院端订单的主键
        orderInfo.setNumber(number);
        orderInfo.setFetchTime(fetchTime);
        orderInfo.setFetchAddress(fetchAddress);
        orderInfo.setAmount(scheduleOrderVo.getAmount());
        orderInfo.setQuitTime(scheduleOrderVo.getQuitTime());//当前订单的最后截止退号时间
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());//订单状态


        baseMapper.insert(orderInfo);


        //3、向第一个队列中发送消息
        // scheduleId + 两个num（医院端返回的） + 就诊人手机号 + 短信内容
        OrderMqVo orderMqVo = new OrderMqVo();
        orderMqVo.setScheduleId(scheduleId);
        orderMqVo.setReservedNumber(reservedNumber);
        orderMqVo.setAvailableNumber(availableNumber);

        MsmVo msmVo = new MsmVo();
        msmVo.setPhone(patient.getPhone());
//        msmVo.setParam();
        msmVo.getParam().put("message","【尚医通】订单创建成功！");
        orderMqVo.setMsmVo(msmVo);//就诊人手机号 + 短信内容


        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,MqConst.ROUTING_ORDER,orderMqVo);

        return orderInfo.getId();//返回平台端订单id
    }

    @Override
    public OrderInfo getOrder(Long orderId) {
        return null;
    }
}

