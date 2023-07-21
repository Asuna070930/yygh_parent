package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/5 10:47
 */
//不需要继承 IService
public interface HospitalService {

    void saveHospital(Map<String, Object> map);//CTRL+ALT+B 转到实现方法

    Hospital show(Map<String, Object> map);

    Page<Hospital> pageList(HospitalQueryVo hospitalQueryVo, Integer pageNum, Integer pageSize);

    void updateStatus(String id, Integer status);

    Map showDetail(String id);

    List<Hospital> findByHosname(String keyword);

    Map findByHoscode(String hoscode);

    Hospital getHospital(String hoscode);
}
