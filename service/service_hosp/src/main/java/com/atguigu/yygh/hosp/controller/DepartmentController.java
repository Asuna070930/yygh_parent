package com.atguigu.yygh.hosp.controller;

import com.atguigu.common.result.R;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/8 19:57
 */
@RestController
@RequestMapping("/admin/hosp/department")
public class DeparmentController {

    @Autowired
    DepartmentService departmentService;

    /**
     * 根据医院编号，查询大科室集合，前端显示科室列表树tree
     *
     * @param hoscode
     * @return
     */
    @GetMapping("/departmentList/{hoscode}")
    public R departmentList(@PathVariable String hoscode) {
        //大科室列表，每一个大科室具备children小科室集合
        List<DepartmentVo> list = departmentService.departmentVoList(hoscode);
        return R.ok().data("list", list);
    }
}
