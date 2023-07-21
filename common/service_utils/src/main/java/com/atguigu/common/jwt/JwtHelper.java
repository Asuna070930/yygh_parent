package com.atguigu.common.jwt;

import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/10 16:39
 */
public class JwtHelper {

    //令牌的过期时间（24h）
    private static long tokenExpiration = 24 * 60 * 60 * 1000;

    //秘钥（自定义字符串）
    private static String tokenSignKey = "123456";


    //使用userId和userName值创建jwt令牌
    public static String createToken(Long userId, String userName) {
        String token = Jwts.builder()
                .setSubject("YYGH-USER")//令牌面向的主体，自定义的名称即可（YYGH-USER，预约挂号用户模块）
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))//设置令牌的过期时间

                .claim("userId", userId) //令牌载荷中的自定义参数
                .claim("userName", userName)
//                .claim("userName", userName)
//                .claim("userName", userName)

                .signWith(SignatureAlgorithm.HS512, tokenSignKey)//令牌的加密算法，加密时使用了tokenSignKey
                .compressWith(CompressionCodecs.GZIP)//令牌压缩格式
                .compact();
        return token;
    }


    //从令牌中解析userId，解析的过程中，抛出异常，说明这个令牌是伪造的
    public static Long getUserId(String token) {
        if (StringUtils.isEmpty(token)) return null;

        //解析令牌时，也需要指定秘钥
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);

        Claims claims = claimsJws.getBody();//他包含了创建令牌时指定的自定义参数

        Integer userId = (Integer) claims.get("userId");
        return userId.longValue();
    }

    //从令牌中解析userName
    public static String getUserName(String token) {
        if (StringUtils.isEmpty(token)) return "";
        Jws<Claims> claimsJws
                = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return (String) claims.get("userName");
    }

    public static void main(String[] args) {
        String token = "eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAAAKtWKi5NUrJSiox099ANDXYNUtJRSq0oULIyNLOwMDM2MjQ31lEqLU4t8kxRsjIyh7D9EnNTgXoMjQ0NTEwMzMwszZRqAWdjTBtJAAAA.u45tOjSk5Lv-IV8DIoSfbPwmxqnAVss1218l4NgW9jFh0iT7u_jHl8zw-mj_4KKcGkJVbeokt93PGILryrt4kQ";
        Long userId = JwtHelper.getUserId(token);
        System.out.println(userId);
    }
}
