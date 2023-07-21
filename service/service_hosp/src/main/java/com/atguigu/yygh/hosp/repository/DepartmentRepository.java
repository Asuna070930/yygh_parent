package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/5 10:43
 */

@Repository
public interface DepartmentRepository extends MongoRepository<Department, String> {

    //根据医院编号和科室编号查询某个具体的科室
    Department findByHoscodeAndDepcode(String hoscode, String depcode);

    void removeByHoscodeAndDepcode(String hoscode, String depcode);

    //根据医院编号，查询该医院下所有的小科室
    List<Department> findByHoscode(String hoscode);
}
