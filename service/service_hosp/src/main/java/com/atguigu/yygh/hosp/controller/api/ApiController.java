package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.common.result.Result;
import com.atguigu.common.utils.HttpRequestHelper;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/5 10:35
 */
//给医院端的 8 个接口
@RestController
@RequestMapping("/api/hosp/")
public class ApiController {

    @Autowired
    HospitalService hospitalService;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    ScheduleService scheduleService;

    /**
     * 上传医院接口
     * 需求：
     * 1、医院端可以重复调用该接口，实现上传医院信息或者修改医院信息
     * 2、尚医通端开发的8个接口中都需要对医院端传递过来的sign签名进行校验，并且医院端传递的sign是经过了md5加密的
     * 如何校验？从平台端的数据库中查询到该医院正确的签名，和医院端传递的签名进行比较（这个过程就是验签，为了保证安全性）
     * 3、医院端传递的logoData表示医院的图片，对应类型是字符串；该接口中需要将该字符串中的所有的空格替换成+     “ ” =》 “+”
     * 4、该接口中无论是添加还是修改医院，status状态默认设置成1
     * 5、8个接口都需要校验该医院是否开通了权限, 不满足要求，则抛出异常
     * 6、对必要参数的非空校验，不满足要求，则抛出异常
     *
     * @param request
     * @return
     */
    //上传医院
    @PostMapping("saveHospital")
    public Result saveHospital(HttpServletRequest request) {

        //获取医院端传递的所有的参数，map中的value其实只有一个值；
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        hospitalService.saveHospital(map);

        return Result.ok();//{code:200,message:"成功"}
//        return Result.fail();
    }

    //上传科室
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        departmentService.saveDepartment(map);
        return Result.ok();
    }

    //上传排班
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        scheduleService.saveSchedule(map);
        return Result.ok();
    }

    /*
     * 需求:查询某个医院 ,将业务数据赋值给data属性
     * */
    @PostMapping("hospital/show")
    public Result hospitalShow(HttpServletRequest request) {
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        Hospital hospital = hospitalService.show(map);
        return Result.ok(hospital);
    }

    //查询科室
    @PostMapping("department/list")
    public Result departmentList(HttpServletRequest request){
        //接收医院端的参数（分页参数+查询条件）
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        //service方法直接返回分页查询的结果
        Page<Department> page = departmentService.departmentList(map);
        //结果赋值给data属性
        return Result.ok(page);
    }

    //查询排版
    @PostMapping("schedule/list")
    public Result scheduleList(HttpServletRequest request){
        //接收医院端的参数（分页插件+查询条件 hoscode depcode）
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        //service方法直接返回分页查询的结果
        Page<Schedule> page = scheduleService.scheduleList(map);
        //结果赋值给data属性
        return Result.ok(page);
    }

    //删除科室
    @PostMapping("department/remove")
    public Result departmentRemove(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        departmentService.remove(map);
        return Result.ok();
    }

    //删除排班
    @PostMapping("schedule/remove")
    public Result scheduleRemove(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        scheduleService.remove(map);
        return Result.ok();
    }
}
