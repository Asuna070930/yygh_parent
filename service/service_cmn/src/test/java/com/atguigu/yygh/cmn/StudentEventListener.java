package com.atguigu.yygh.cmn;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/3 14:32
 */
public class StudentEventListener extends AnalysisEventListener<Student> {

    //逐行读取excel表格中的数据
    @Override
    public void invoke(Student student, AnalysisContext context) {
        System.out.println("当前行数据" + student);
    }

    //所有行都读取完毕之后执行一次
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        System.out.println("所有行读取完毕");
    }
}
