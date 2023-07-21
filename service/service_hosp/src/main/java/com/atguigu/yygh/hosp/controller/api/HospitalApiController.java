package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.common.result.R;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/10 9:13
 */
//给挂号网站使用
@RestController
@RequestMapping("/api/hosp")
public class HospitalApiController {
    @Autowired
    HospitalService hospitalService;

    @Autowired
    DepartmentService departmentService;


    @Autowired
    ScheduleService scheduleService;

    @GetMapping("getSchedule/{schdeuleId}")
    public R getSchedule(@PathVariable String schdeuleId) {
        Schedule schedule = scheduleService.getSchedule(schdeuleId);
        return R.ok().data("schedule", schedule);
    }


    /**
     * 点击日期，查询该日期下的排班列表
     *
     * @param hoscode
     * @param depcode
     * @param workDate
     * @return
     */
    @GetMapping("getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public R getScheduleDetail(@PathVariable String hoscode, @PathVariable String depcode, @PathVariable String workDate) {
        List<Schedule> list = scheduleService.getScheduleDetail(hoscode, depcode, workDate);
        return R.ok().data("scheduleList", list);//response.data.scheduleList
    }


    /**
     * 挂号网站点击某个小科室，需要调用的接口,统计排班日期列表 + total总日期个数+baseMap（医院名称+大小科室名称）
     *
     * @return
     */
    @GetMapping("getScheduleBookingRule/{hoscode}/{depcode}/{pageNum}/{pageSize}")
    public R getScheduleBookingRule(@PathVariable String hoscode, @PathVariable String depcode, @PathVariable Integer pageNum, @PathVariable Integer pageSize) {
        //map = 排班日期ruleVo集合 + total总日期个数 + 其它
        //没有封装
//        Map map = scheduleService.getScheduleBookingRule(hoscode,depcode,pageNum,pageSize);
        //封装
        Map map = scheduleService.getScheduleBookingRule1(hoscode, depcode, pageNum, pageSize);
        return R.ok().data(map);
    }


    /**
     * 显示科室列表
     *
     * @param hoscode
     * @return
     */
    @GetMapping("findDepartmentList/{hoscode}")
    public R findDepartmentList(@PathVariable String hoscode) {
        //大科室列表
        List<DepartmentVo> departmentVoList = departmentService.departmentVoList(hoscode);
        return R.ok().data("list", departmentVoList);
    }


    /**
     * 根据医院编号查询医院详情
     *
     * @param hoscode
     * @return
     */
    @GetMapping("show/{hoscode}")
    public R show(@PathVariable String hoscode) {
        Map map = hospitalService.findByHoscode(hoscode);
        return R.ok().data(map);//hospital + bookingRule
    }


    /**
     * 首页查询医院列表
     */
    @GetMapping("{page}/{limit}") // 如果使用getmapping，就不能使用@RequestBody，前端使用params属性传递查询条件
    public R index(@PathVariable Integer page, @PathVariable Integer limit, HospitalQueryVo hospitalQueryVo) {
        hospitalQueryVo.setStatus(1);//只加载上线状态的医院列表
        Page<Hospital> hospitalPage = hospitalService.pageList(hospitalQueryVo, page, limit);
//        List<Hospital> content = hospitalPage.getContent();
//        int totalPages = hospitalPage.getTotalPages();
        return R.ok().data("pages", hospitalPage);
    }


    /**
     * 根据输入的关键词, 模糊查询医院列表
     *
     * @param keyword
     * @return
     */
    @GetMapping("getByHosname/{keyword}")
    public R hospList(@PathVariable String keyword) {
        List<Hospital> list = hospitalService.findByHosname(keyword);
        return R.ok().data("list", list);
    }
}
