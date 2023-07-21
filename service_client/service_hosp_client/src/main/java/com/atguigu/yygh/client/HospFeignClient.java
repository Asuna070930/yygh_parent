package com.atguigu.yygh.client;

import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/15 10:38
 */
@FeignClient(value = "service-hosp")
public interface HospFeignClient {

    @GetMapping("/admin/hosp/schedule/getSchedule/{scheduleId}")
    public ScheduleOrderVo getSchedule(@PathVariable String scheduleId);


    /**
     * 根据医院编号查询apiUrl
     * @param hoscode
     * @return
     */
    @GetMapping("/admin/hosp/hospset/getApiUrl/{hoscode}")
    public String getApiUrl(@PathVariable String hoscode);
}

