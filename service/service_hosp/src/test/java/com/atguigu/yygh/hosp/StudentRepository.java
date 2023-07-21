package com.atguigu.yygh.hosp;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends MongoRepository<Student,String> {

    //age = 20
    //自定义查询，删除方法
    // findBy  removeBy
    List<Student> findByAgeEquals(Integer age);

    //age > 20
    List<Student> findByAgeGreaterThan(Integer age);

    // age <= 20 and address = 重庆
    List<Student> findByAgeLessThanEqualAndAddressEquals(Integer age,String address);

    // age in ( 19 ,20,21)
    List<Student> findByAgeIn(List<Integer> ids);

    // age between 10 and 20
    List<Student> findByAgeBetween(Integer age1,Integer age2);


    //删除age>?
    void removeByAgeGreaterThan(Integer age);


}
