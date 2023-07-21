package com.atguigu.yygh.user.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.exphandler.YyghException;
import com.atguigu.common.jwt.JwtHelper;
import com.atguigu.common.result.R;
import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.config.ConstantPropertiesUtil;
import com.atguigu.yygh.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/11 14:47
 */
@Controller
@RequestMapping("/api/user/wx")//路径的第二段/user
public class WeixinApiController {


    //    @Bean
//    public RestTemplate restTemplate(){
//        return new RestTemplate();
//    }
    @Autowired
    RestTemplate restTemplate;


    @Autowired
    UserInfoService userInfoService;


    /**
     * 扫码之后点击允许，（微信端）自动调用
     *
     * 需求：
     *   1、获取微信端的code临时令牌，参数格式：redirect_uri?code=CODE
     *   2、根据code临时令牌，去调用微信端接口获取openid 和 accessToken(为了继续获取该微信的昵称)
     *   3、根据accessToken获取该微信昵称nickname
     *   4、先判断该微信用户在user_info表中是否存在，如果不存在 利用openid + nickname  自动注册
     *   5、判断用户的status是否被锁定，如果用户被锁定，抛出自定义异常
     *   6、准备name和token，
     *       name=userinfo.name   or  userinfo.nickname  or userinfo.phone   和之前的规则一样
     *       token = name + userinfo.id 创建的一个jwt令牌  和之前的规则一样
     *   7、return  重定向到前端的callback.vue 并且传递参数 url?name=xx&token=xxx&openid=abc
     *                      如果该微信用户的phone字段为空，abc填写实际的openid值
     *                      如果该微信用户的phone字段不为空 abc填写空字符串即可
     *
     *    @RestController 替换成@Controller
     */
    @GetMapping("callback") //redirectUri
    public String callback(String code) throws UnsupportedEncodingException {

        System.out.println("code临时票据=" + code);

        //根据临时令牌获取openid和accessToken
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                "appid=" + ConstantPropertiesUtil.APPID +
                "&secret=" + ConstantPropertiesUtil.APPSECRET +
                "&code=" + code +
                "&grant_type=authorization_code";

        //返回值格式：
        /*
        {
            "access_token":"ACCESS_TOKEN",
            "expires_in":7200,
            "refresh_token":"REFRESH_TOKEN",
            "openid":"OPENID",
            "scope":"SCOPE",
            "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
            }
        * */

        //使用restTemplate 发get请求
        String json = restTemplate.getForObject(url, String.class);
//        System.out.println(json);
//        json字符串转码map、JSONObject 、JSON ...
        JSONObject jsonObject = JSON.parseObject(json);

        String openid = jsonObject.getString("openid");
        String access_token = jsonObject.getString("access_token");


        //根据openid去user_info表中查询该用户是否存在
        UserInfo userInfo = userInfoService.selectByOpenid(openid);

        if(userInfo==null){
            //不存在，需要自动注册
            userInfo = new UserInfo();

            //获取nickname
            String urlNick = "https://api.weixin.qq.com/sns/userinfo?" +
                    "access_token=" + access_token +
                    "&openid=" + openid;

            String forObject = restTemplate.getForObject(urlNick, String.class);
            JSONObject object = JSON.parseObject(forObject);

            String nickname = object.getString("nickname");

            userInfo.setOpenid(openid);
            userInfo.setNickName(nickname);
            userInfo.setStatus(1);//正常
            userInfo.setAuthStatus(AuthStatusEnum.NO_AUTH.getStatus());//0：未认证

            userInfoService.save(userInfo);
        }

        //当前微信用户被锁定
        if(userInfo.getStatus()==0){
            throw new YyghException(20001,"当前微信用户被锁定");
        }

        //准备name和token
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)){
            name = userInfo.getNickName();
            if(StringUtils.isEmpty(name)){
                name = userInfo.getPhone();
            }
        }

        String token = JwtHelper.createToken(userInfo.getId(),name);

        String abc = "";
        if(StringUtils.isEmpty(userInfo.getPhone())){
            abc = openid;
        }

        //return
//        return "redirect:http://localhost:3000/weixin/callback"; /pages/weixin/callback.vue

        //url中的中文参数
        return "redirect:http://localhost:3000/weixin/callback?name="+ URLEncoder.encode(name, "utf-8")+"&token="+token+"&openid=" + abc;
    }

    /**
     * 前端的二维码所需要的参数
     * @return
     * @throws UnsupportedEncodingException
     */
    @ResponseBody
    @GetMapping("/getLoginParam")
    public R getLoginParam() throws UnsupportedEncodingException {
        Map<String,Object> map = new HashMap<>();

        map.put("self_redirect",true);//true：弹出框显示二维码
        map.put("id","weixinLogin");//显示二维码的前端容器的id
        map.put("scope","snsapi_login");//网页应用固定snsapi_login
        map.put("appid", ConstantPropertiesUtil.APPID);// 这两个参数需要在微信开放平台申请    应用id
        map.put("redirect_uri", URLEncoder.encode(ConstantPropertiesUtil.REDIRECTURL,"utf-8"));// 重定向地址

        return R.ok().data(map);
    }

}
