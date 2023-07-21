package com.atguigu.yygh.user.feign;

import com.atguigu.yygh.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/15 9:15
 */
@FeignClient(value = "service-user")
public interface PatientFeignClient {

    @GetMapping("/api/user/patient/{patientId}")
    public Patient getPatient(@PathVariable Long patientId);
}

