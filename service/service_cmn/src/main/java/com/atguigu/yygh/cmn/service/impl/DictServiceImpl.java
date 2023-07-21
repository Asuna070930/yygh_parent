package com.atguigu.cmn.service.impl;

import com.atguigu.cmn.mapper.DictMapper;
import com.atguigu.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/3 9:57
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper,Dict> implements DictService{
}
