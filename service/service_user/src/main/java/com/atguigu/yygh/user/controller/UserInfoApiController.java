package com.atguigu.yygh.user.controller;

import com.atguigu.common.exphandler.YyghException;
import com.atguigu.common.jwt.AuthContextHolder;
import com.atguigu.common.jwt.JwtHelper;
import com.atguigu.common.result.R;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/10 15:23
 */

//挂号网站使用的用户登录接口
@RestController
@RequestMapping("/api/user")
public class UserInfoApiController {

    @Autowired
    UserInfoService userInfoService;


    /**
     * 根据请求头中的令牌，查询当前用户信息
     * 前端会根据用户信息中的authStatus的值的情况，决定接下来跳转到排班页面，还是认证中心页面
     * @param request
     * @return
     */
    @GetMapping("getUserInfo")
    public R getUserInfo(HttpServletRequest request){
//        String token = request.getHeader("token");//从请求头中获取某个值
        //解析令牌，如果出现异常，说明令牌不正确
        try {
//            Long userId = JwtHelper.getUserId(token);
            Long userId = AuthContextHolder.getUserId(request);
            //根据id更新
            UserInfo userInfo = userInfoService.getUserInfo(userId);
            return R.ok().data("userInfo",userInfo);
        } catch (Exception e) {
            throw new YyghException(20001,"令牌不合法");
        }
    }



    /**
     * 提交认证信息  真实姓名+证件类型+证件编号+图片路径
     * 更新的操作，根据用户的id，更新4个字段的值
     * UserAuthVo 存储4个字段
     * 缺少一个用户的id？
     *
     *
     * 前端：token--》cookie中；  前端发起请求时，将token放在请求头中（名称=token），传递给后端； 后端接口如何获取令牌？ 从请求头获取
     * @return
     */
    @PostMapping("userAuth")
    public R userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request){

        String token = request.getHeader("token");//从请求头中获取某个值
        //解析令牌，如果出现异常，说明令牌不正确
        try {
            Long userId = JwtHelper.getUserId(token);
            //根据id更新
            userInfoService.userAuth(userId,userAuthVo);
        } catch (Exception e) {
            throw new YyghException(20001,"令牌不合法");
        }

        return R.ok();
    }


    /**
     * 两个业务逻辑： 1、手机号+短信验证码 登录  loginVo = 手机号phone + 短信验证码code
     * 2、为微信用户绑定手机号
     *
     * @return
     */
    @PostMapping("login")
    public R login(@RequestBody LoginVo loginVo) {

        Map map = null;
        if (StringUtils.isEmpty(loginVo.getOpenid())) {
            //直接通过手机号+验证码登录
            map = userInfoService.login(loginVo);//map=name + token
        } else {
            //为微信用户绑定手机号
            map = userInfoService.bundle(loginVo);//map = name + token
        }

//        return R.ok().data("token",map.get("token")).data("name",map.get("name"));
        return R.ok().data(map);
    }
}
