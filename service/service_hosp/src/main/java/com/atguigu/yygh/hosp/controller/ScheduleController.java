package com.atguigu.yygh.hosp.controller;

import com.atguigu.common.result.R;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/9 8:53
 */
@RestController
@RequestMapping("/admin/hosp/schedule")
public class ScheduleController {

    @Autowired
    ScheduleService scheduleService;


    @Autowired
    HospitalService hospitalService;

    @Autowired
    DepartmentService departmentService;


    @Autowired
    HospitalSetService hospitalSetService;

    /**
     * ScheduleOrderVo包括了哪些数据？1、调用医院端接口时  2、创建平台端订单时
     * @param scheduleId
     * @return
     */
    @GetMapping("getSchedule/{scheduleId}")
    public ScheduleOrderVo getSchedule(@PathVariable String scheduleId){

        //根据排班id查询排班对象
        Schedule schedule = scheduleService.getSchedule(scheduleId);


        //实例化一个scheduleOrderVo（调用医院端接口时所需的排班属性+创建品台端订单时所需要的属性）
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();

        //根据hoscode医院编号查询医院
        Hospital hospital = hospitalService.getHospital(schedule.getHoscode());


        //查询科室
        Department department = departmentService.getDepartment(schedule.getHoscode(),schedule.getDepcode());

        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospital.getHosname());
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(department.getDepname());
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setAmount(schedule.getAmount());

        //就诊日的前一天（-1）的 15:30
        Date workDate = schedule.getWorkDate();
        DateTime dateTime = new DateTime(workDate).plusDays(hospital.getBookingRule().getQuitDay());
        String s = dateTime.toString("yyyy-MM-dd");
        Date date = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(s + " " + hospital.getBookingRule().getQuitTime()).toDate();

        scheduleOrderVo.setQuitTime(date);//最后截止退号时间

        //今天的8:30 对应的Date对象
        scheduleOrderVo.setStartTime(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(new DateTime().toString("yyyy-MM-dd") + " " + hospital.getBookingRule().getReleaseTime()).toDate());//今天开始挂号时间

        //第cycle天的 11:30 （第10天的11:30）
        scheduleOrderVo.setEndTime(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(new DateTime().plusDays(hospital.getBookingRule().getCycle()).toString("yyyy-MM-dd") + " " + hospital.getBookingRule().getStopTime()).toDate());//预约周期内的截止挂号时间


        scheduleOrderVo.setStopTime(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(new DateTime().toString("yyyy-MM-dd") + " " + hospital.getBookingRule().getStopTime()).toDate());//今天停止挂号时间


        //根据医院编号查询医院设置
        HospitalSet hospitalSet = hospitalSetService.getByHoscode(hospital.getHoscode());
        scheduleOrderVo.setApiUrl(hospitalSet.getApiUrl());//医院的接口路径（从医院设置表中查询）

        return scheduleOrderVo;
    }


    /**
     * 根据医院编号+科室编号+分页参数，查询该科室下的排班日期列表 + 其它数据
     */
    @GetMapping("getScheduleRuleVoList/{page}/{limit}/{hoscode}/{depcode}")
    public R getScheduleRuleVoList(@PathVariable Long page,
                                   @PathVariable Long limit,
                                   @PathVariable String hoscode,
                                   @PathVariable String depcode) {

        //map中一共4个数据（今天实现第一个日期集合数据）
//        Map map = scheduleService.getScheduleRuleVoList(page,limit,hoscode,depcode);
        Map map = scheduleService.getScheduleRuleVoList2(page, limit, hoscode, depcode);
        return R.ok().data(map);//map = bookingScheduleRuleVoList
    }

    /**
     * 根据日期查询排班列表
     */
    @GetMapping("getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public R getScheduleDetail(@PathVariable String hoscode, @PathVariable String depcode, @PathVariable String workDate) {
        List<Schedule> list = scheduleService.getScheduleDetail(hoscode, depcode, workDate);
        return R.ok().data("list", list);
    }

}
