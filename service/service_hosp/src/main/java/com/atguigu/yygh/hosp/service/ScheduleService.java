package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.order.OrderMqVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/5 10:51
 */
public interface ScheduleService {
    void saveSchedule(Map<String, Object> map);

    Page<Schedule> scheduleList(Map<String, Object> map);

    void remove(Map<String, Object> map);

    Map getScheduleRuleVoList(Long page, Long limit, String hoscode, String depcode);

    List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate);

    Map getScheduleRuleVoList2(Long page, Long limit, String hoscode, String depcode);

    Map getScheduleBookingRule(String hoscode, String depcode, Integer pageNum, Integer pageSize);

    Map getScheduleBookingRule1(String hoscode, String depcode, Integer pageNum, Integer pageSize);


    Schedule getSchedule(String schdeuleId);

    void updateSchedule(OrderMqVo orderMqVo);
}
