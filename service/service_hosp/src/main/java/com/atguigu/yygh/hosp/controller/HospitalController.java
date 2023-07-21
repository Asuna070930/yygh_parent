package com.atguigu.yygh.hosp.controller;

import com.atguigu.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/6 15:36
 */
//@CrossOrigin
//给后端系统使用
@RestController
@RequestMapping("/admin/hosp/hospital")
public class HospitalController {

    @Autowired
    HospitalService hospitalService;


    /**
     * 查询医院的基本信息+预约规则信息
     * @param id
     * @return
     */
    @GetMapping("show/{id}")
    public R show(@PathVariable("id") String id) {
        Map map = hospitalService.showDetail(id);
//        map = hospital + bookingRule
        return R.ok().data("item", map);
    }


    //更新医院状态
    @GetMapping("updateStatus/{id}/{status}")
    public R updateStatus(@PathVariable String id, @PathVariable Integer status) {
        hospitalService.updateStatus(id, status);
        return R.ok();
    }


    /**
     * 后台系统的医院列表
     *
     * @return
     */
    @PostMapping("/hospList/{pageNum}/{pageSize}")
    public R hospList(@RequestBody HospitalQueryVo hospitalQueryVo, @PathVariable Integer pageNum, @PathVariable Integer pageSize) {

        //mongodb分页查询的返回结果类 R（spring-data包下的）
        Page<Hospital> page = hospitalService.pageList(hospitalQueryVo, pageNum, pageSize);

        //List<Hospital> content = page.getContent();//当前页的结果集
        //long totalElements = page.getTotalElements();//总记录数
        //int totalPages = page.getTotalPages();//总页数
        //boolean b = page.hasPrevious();
        //boolean b1 = page.hasNext();

        return R.ok().data("pages", page);//前端如何解析？ resp.data.pages.content  resp.data.pages.totalElements
        //return R.ok().data("list",content).data("total",totalElements);//当前页的结果集 + 总记录数
    }

    /**
     * 后台系统的医院列表
     *
     * @return
     */
    @GetMapping("/hospList/{pageNum}/{pageSize}")
    public R hospList2(HospitalQueryVo hospitalQueryVo, @PathVariable Integer pageNum, @PathVariable Integer pageSize) {
        Page<Hospital> page = hospitalService.pageList(hospitalQueryVo, pageNum, pageSize);
        return R.ok().data("pages", page);//前端如何解析？ resp.data.pages.content  resp.data.pages.totalElements
    }
}
