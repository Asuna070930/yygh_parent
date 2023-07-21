package com.atguigu.yygh.msm.controller;


import com.atguigu.common.result.R;
import com.atguigu.yygh.msm.service.MsmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/msm")
public class MsmController {

    @Autowired
    MsmService msmService;

    /**
     * 给指定的手机号发送一个4位的纯数字的短信验证码，并且发送成功后，将手机号+code作为一组k-v存入到redis，存储5分钟
     * 附加需求：5分钟内，该接口只为某个手机号发送一个验证码
     * @param phone
     * @return
     */
    @GetMapping("sendCode/{phone}")
    public R sendCode(@PathVariable String phone){
        msmService.send(phone);
        return R.ok();
    }
}
