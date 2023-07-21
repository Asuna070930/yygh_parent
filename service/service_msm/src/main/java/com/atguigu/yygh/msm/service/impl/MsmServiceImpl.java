package com.atguigu.yygh.msm.service.impl;

import com.atguigu.common.msm.HttpUtils;
import com.atguigu.yygh.msm.service.MsmService;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/10 20:08
 */
@Service
public class MsmServiceImpl implements MsmService {


    @Autowired
    StringRedisTemplate stringRedisTemplate;


    @Override
    public void send(String phone) {

        //判断验证码是否已发送
        String code = stringRedisTemplate.opsForValue().get(phone);
        if(!StringUtils.isEmpty(code)){
            //验证码已经存在，不需要重复发送
            return;
        }

        //随机验证码
        code = this.getCode();

        //第三方短信接口域名
        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";

        String appcode = "40b1f46f7a894f3a84f327181bb035e5";

        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);
        querys.put("param", "**code**:" + code + ",**minute**:5"); //注意：纯数字

        //smsSignId（短信前缀）和templateId（短信模板），可登录国阳云控制台自助申请。参考文档：http://help.guoyangyun.com/Problem/Qm.html
        // 【国阳云】xxxxx
        querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
        querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
        Map<String, String> bodys = new HashMap<String, String>();

        try {
            /**
             * 重要提示如下:
             * HttpUtils请从\r\n\t    \t*
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java\r\n\t    \t* 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));

            //存储到redis中
            stringRedisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCode() {
        long c = (long) (Math.random() * 10000);
        System.out.println(c);
        if (c < 1000) {
            c += 1000;
        }
        return c + "";
    }

//    public static void main(String[] args) {
//        String code = getCode();
//        System.out.println(code);
//    }
}
