package com.atguigu.yygh.hosp;

import com.atguigu.yygh.hosp.mapper.HospitalSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/6/28 20:50
 */
@SpringBootTest
public class MapperXmlTest {
    @Autowired
    HospitalSetMapper hospitalSetMapper;

//    @Test
//    public void test1() {
//        Integer integer = hospitalSetMapper.calCount();
//        System.out.println(integer);
//    }
}
