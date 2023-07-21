package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/5 10:44
 */
@Repository
public interface ScheduleRepository extends MongoRepository<Schedule, String> {

    //医院编号+科室编号+排班编号
    Schedule findByHoscodeAndDepcodeAndHosScheduleId(String hoscode, String depcode, String hosScheduleId);

    //医院编号+排班编号
    Schedule findByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);

    // 注意：方法的格式
    //  findBy      Hoscode  And  Depcode   And   WorkDate
    //方法参数类型：必须和实体类中的属性类型一致
    List<Schedule> findByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date workDate);

    //查询某个医院某个科室下所有的排班,并且排序
    List<Schedule> findByHoscodeAndDepcodeOrderByWorkDateDesc(String hoscode, String depcode);

}

