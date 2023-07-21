package com.atguigu.yygh.user.controller;

import com.atguigu.common.exphandler.YyghException;
import com.atguigu.common.jwt.AuthContextHolder;
import com.atguigu.common.result.R;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/13 9:23
 */
@RestController
@RequestMapping("/api/user/patient")
public class PatientController {


    @Autowired
    PatientService patientService;


    /**
     * 订单服务使用的，根据就诊人i给d查询就诊人
     * @param patientId
     * @return
     */
    @GetMapping("{patientId}")
    public Patient getPatient(@PathVariable Long patientId){
        Patient one = patientService.findOne(patientId);
        return one;
    }



    /**
     * 前端查询当前用户的就诊人列表，请求头中携带了token令牌
     * @return
     */
    @GetMapping("/findList")
    public R findList(HttpServletRequest request){
        Long userId = null;
        try {
            userId = AuthContextHolder.getUserId(request);
        } catch (Exception e) {
            throw new YyghException(20001,"令牌不合法");
        }
        List<Patient> list = patientService.findAll(userId);
        return R.ok().data("patientList",list);
    }


    /**
     * 查询某个就诊人详情
     * @param patientId
     * @return
     */
    @GetMapping("/getPatient/{patientId}")
    public R getOne(@PathVariable Long patientId){
        Patient patient = patientService.findOne(patientId);
        return R.ok().data("patient",patient);
    }

    /**
     * 删除就诊人
     * @param patientId
     * @return
     */
    @DeleteMapping("/deleteOne/{patientId}")
    public R deleteOne(@PathVariable Long patientId){
        patientService.removeById(patientId);
        return R.ok();
    }


    /**
     * 添加就诊人
     * @param patient
     * @return
     */
    @PostMapping("/addPatient")
    public R addPatient( @RequestBody Patient patient ,HttpServletRequest request ){
        Long userId = null;
        try {
            userId = AuthContextHolder.getUserId(request);
        } catch (Exception e) {
            throw new YyghException(20001,"令牌不合法");
        }

        //注册账号最多可以同时绑定四个就诊人
        Integer count = patientService.getPatientCount(userId);
        if(count>=4){
            throw new YyghException(20001,"注册账号最多可以同时绑定四个就诊人");
        }

        //注册账号每自然年内，最多可以绑定八位就诊人
        Integer total = patientService.getCountByYear(userId);
        if(total>=8){
            throw new YyghException(20001,"注册账号每自然年内，最多可以绑定八位就诊人");
        }

        //同一手机号，最多同时可以被八位就诊人作为联系电话
        String contactsPhone = patient.getContactsPhone();//当前就诊人的联系人的电话号
        //从就诊人表通过该手机查询一共出现多少次
        Integer phoneCount = patientService.getContactsPhoneCount(contactsPhone);

        if(phoneCount>=8){
            throw new YyghException(20001,"同一手机号，最多同时可以被八位就诊人作为联系电话");
        }


        patient.setUserId(userId);//当前就诊人所属的用户
        patientService.save(patient);
        return R.ok();
    }


    /**
     * 更新就诊人
     * @param patient
     * @return
     */
    @PostMapping("/updatePatient")
    public R updatePatient( @RequestBody Patient patient ){
        //patient中需要传递id，根据id更新
        if(StringUtils.isEmpty(patient.getId())){
            throw new YyghException(20001,"更新就诊人时必须传递id主键");
        }



        //同一手机号，最多同时可以被八位就诊人作为联系电话
        String contactsPhone = patient.getContactsPhone();//当前就诊人的联系人的电话号
        //从就诊人表通过该手机查询一共出现多少次
        Integer phoneCount = patientService.getContactsPhoneCount(contactsPhone);

        if(phoneCount>=8){
            throw new YyghException(20001,"同一手机号，最多同时可以被八位就诊人作为联系电话");
        }

        patientService.updateById(patient);
        return R.ok();
    }


}
