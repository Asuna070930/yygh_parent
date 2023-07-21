package com.atguigu.yygh.user.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/11 15:09
 */
@Component
public class ConstantPropertiesUtil implements InitializingBean {

    @Value("${wx.open.app_id}")
    private String appId;

    @Value("${wx.open.app_secret}")
    private String appSecret;

    @Value("${wx.open.redirect_url}")
    private String redirectUrl;



    public static String APPID;
    public static String APPSECRET;
    public static String REDIRECTURL;



    //这个方法在什么时候执行？  上边的3个属性都赋值成功后会执行
    @Override
    public void afterPropertiesSet() throws Exception {
        APPID = this.appId;
        APPSECRET = this.appSecret;
        REDIRECTURL = this.redirectUrl;
    }
}
