package com.atguigu.yygh.cmn;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/4 15:33
 */
@SpringBootTest
public class RedisTest {
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void test01() {
        redisTemplate.opsForValue().set("username", "tom");
    }

    @Test
    public void test02() {
        String username = (String) redisTemplate.opsForValue().get("username");
        System.out.println(username);
    }

    @Test
    public void test03() {
        stringRedisTemplate.opsForValue().set("useraddress", "北京");
    }

    @Test
    public void test04() {
        String useraddress = stringRedisTemplate.opsForValue().get("useraddress");
        System.out.println(useraddress);
    }

    //使用RedisTemplate 获取 StringRedisTemplate的数据: null
    //注意: 存数据和取数据时 要使用相同类型的template对象
    @Test
    public void test05() {
        String useraddress = (String) redisTemplate.opsForValue().get("useraddress");
        System.out.println(useraddress);
    }
}
