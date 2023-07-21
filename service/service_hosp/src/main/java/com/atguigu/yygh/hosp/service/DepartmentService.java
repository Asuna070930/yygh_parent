package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/5 10:50
 */
public interface DepartmentService {
    void saveDepartment(Map<String, Object> map);

    Page<Department> departmentList(Map<String, Object> map);

    void remove(Map<String, Object> map);

    List<DepartmentVo> departmentVoList(String hoscode);

    Department getDepartment(String hoscode, String depcode);
}
