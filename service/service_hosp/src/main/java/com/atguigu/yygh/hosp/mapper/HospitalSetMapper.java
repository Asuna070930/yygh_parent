package com.atguigu.yygh.hosp.mapper;

import com.atguigu.yygh.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/6/25 18:33
 */
@Repository
public interface HospitalSetMapper extends BaseMapper<HospitalSet> {
    public Integer calCount();
}
