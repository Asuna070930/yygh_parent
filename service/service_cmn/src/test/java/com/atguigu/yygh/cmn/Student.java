package com.atguigu.yygh.cmn;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/3 14:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {

    @ExcelProperty(index = 0,value = "学生编号")
    private Integer stuNum;

    @ExcelProperty(index = 1,value = "学生姓名")
    private String stuName;

    @ExcelProperty(index = 2,value = "学生地址")
    private String address;
}
