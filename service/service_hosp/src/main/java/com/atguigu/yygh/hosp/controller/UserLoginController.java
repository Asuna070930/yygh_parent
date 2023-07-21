package com.atguigu.yygh.hosp.controller;

import com.atguigu.common.result.R;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/6/29 18:57
 */
//@CrossOrigin
@RestController
@RequestMapping("/admin/hosp/user")
public class UserLoginController {
    @PostMapping("/login")
    public R login() {
        return R.ok().data("token", "admin-token"); //注意: code=20000 检查resultcode是否是20000
    }

    @GetMapping("/info")
    public R info() {
        return R.ok().data("roles", Arrays.asList("admin")).data("introduction","I am a super administrator").data("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif").data("name","胡歌");
    }
}
