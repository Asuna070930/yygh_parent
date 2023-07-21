package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/5 10:38
 */
@Repository
public interface HospitalRepository extends MongoRepository<Hospital,String> {
    Hospital findByHoscode(String hoscode);

    //根据医院名称模糊查询
    List<Hospital> findByHosnameLike(String hosname);
}
