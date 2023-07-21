package com.atguigu.yygh.cmn.service;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component// 放在容器中
public class DictReadListener extends AnalysisEventListener<DictEeVo> {

    @Autowired
    DictMapper dictMapper;

    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        //dictEeVo添加到dict表中
        System.out.println(dictEeVo);
        //DictEeVo--》Dict对象
        Dict dict = new Dict();
//        dict.setId(dictEeVo.getId());
//        dict.setParentId(dictEeVo.getParentId());
//        dict.setName(dictEeVo.getName());
//        dict.setDictCode(dictEeVo.getDictCode());
//        dict.setValue(dictEeVo.getValue());
        BeanUtils.copyProperties(dictEeVo, dict);//dictEeVo对象中的属性值赋值给dict对象 ， 注意：属性名一致的才能这样去复制

        dictMapper.insert(dict);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
