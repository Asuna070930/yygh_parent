package com.atguigu.yygh.user.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.msm.HttpUtils;
import com.atguigu.common.result.R;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/13 16:39
 */


//后台管理系统
@RestController
@RequestMapping("/admin/user")
public class UserController {

    @Autowired
    UserInfoService userInfoService;


    @GetMapping("/checkIdCardAndName/{idCard}/{name}")
    public R checkIdCardAndName(@PathVariable String idCard , @PathVariable String name){

        String host = "https://dfidveri.market.alicloudapi.com";
        String path = "/verify_id_name";
        String method = "POST";
        String appcode = "40b1f46f7a894f3a84f327181bb035e5";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("id_number", idCard);
        bodys.put("name", name);
        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            //  System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
            String s = EntityUtils.toString(response.getEntity());
            System.out.println(s);
            JSONObject jsonObject= JSON.parseObject(s);
            Integer state = jsonObject.getInteger("state");
            if (state==1){
                return R.ok().message("一致");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.ok().message("不一致");
    }


    /**
     * 用户的审核（实名认证审核）
     *
     * @param userId
     * @param authStatus
     * @return
     */
    @GetMapping("updateAuthStatus/{userId}/{authStatus}")
    public R updateAuthStatus(@PathVariable Long userId, @PathVariable Integer authStatus) {
        userInfoService.updateAuthStatus(userId, authStatus);
        return R.ok();
    }

    /**
     * 用户列表，点击查看按钮，查询当前用户的详细信息
     *
     * @param userId
     * @return
     */
    @GetMapping("show/{userId}")
    public R show(@PathVariable Long userId) {
        Map map = userInfoService.show(userId);//map = userInfo + patientList
        return R.ok().data(map);
    }

    /**
     * 根据id修改status
     *
     * @param id
     * @param status
     * @return
     */
    @GetMapping("/updateStatus/{id}/{status}")
    public R updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        userInfoService.updateStatus(id, status);
        return R.ok();
    }

    /**
     * 后台管理系统查询用户列表
     *
     * @param page
     * @param limit
     * @param userInfoQueryVo
     * @return
     */
    @PostMapping("selectList/{page}/{limit}")
    public R selectList(@PathVariable Integer page, @PathVariable Integer limit, @RequestBody UserInfoQueryVo userInfoQueryVo) {

        //注意：mybatisplus的分页查询(PaginationInterceptor)，返回的Page对象
//        Page<UserInfo> pageResult = userInfoService.selectList(page, limit, userInfoQueryVo);
        Page<UserInfo> pageResult = userInfoService.selectList(page, limit, userInfoQueryVo);

        List<UserInfo> records = pageResult.getRecords();//当前页结果集
        long total = pageResult.getTotal();//总记录数

        return R.ok().data("list", records).data("total", total);
    }

}
