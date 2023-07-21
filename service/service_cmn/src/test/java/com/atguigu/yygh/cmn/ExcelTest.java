package com.atguigu.yygh.cmn;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/3 14:04
 */
@SpringBootTest
public class ExcelTest {
    @Test
    public void test01() throws FileNotFoundException {

        //创建3个stu对象,写入到桌面的"学生.xlsx"
        Student student1 = new Student(101, "胡歌", "桃花岛");
        Student student2 = new Student(102, "刘亦菲", "桃花岛");
        Student student3 = new Student(103, "saber", "乌托邦");

        List<Student> students = Arrays.asList(student1, student2, student2);

        //输出流,只想桌面的excel文件
        String path = "C:\\Users\\95680\\Desktop\\学生.xlsx";
        FileOutputStream outputStream = new FileOutputStream(path);
        EasyExcel.write(outputStream, Student.class).sheet("成都").doWrite(students);

    }


    @Test
    public void test02() throws FileNotFoundException {

        //1.指定excel文档存放地址
        String path = "C:\\Users\\95680\\Desktop\\demo.xlsx";
        FileOutputStream outputStream = new FileOutputStream(path);

        //2.准备多个sheet表对应的数据集合
        Student student1 = new Student(101, "胡歌", "桃花岛");
        Student student2 = new Student(102, "刘亦菲", "桃花岛");
        Student student3 = new Student(103, "saber", "乌托邦");
        //2.1 第一个sheet表对应的数据集合
        List<Student> studentList1 = Arrays.asList(student1, student2, student3);

        //重庆学生列表
        Student student4 = new Student(104, "lin", "理想乡");
        Student student5 = new Student(105, "shilang", "理想乡");
        Student student6 = new Student(106, "asuna", "理想乡");
        //2.2 第二个sheet表对应的数据集合
        List<Student> studentList2 = Arrays.asList(student4, student5, student6);

        //3.开始创建ExcelWriter   参数1:文件输出流程  参数2: class类型
        ExcelWriter excelWriter = EasyExcel.write(outputStream, Student.class).build();

        //4.开始创建sheet对象 (两种方法创建sheet对象)
        WriteSheet writeSheet1 = new WriteSheet();
        writeSheet1.setSheetName("成都学生列表");

        WriteSheet writeSheet2 = new WriteSheet();
        writeSheet2.setSheetName("重庆学生列表");

        //5.excelWriter调用write方法   参数1: 数据集合  参数2: 对应的sheet表
        excelWriter.write(studentList1, writeSheet1);
        excelWriter.write(studentList2, writeSheet2);

        //6.最后finish方法
        excelWriter.finish();
    }

    @Test
    public void test03() throws FileNotFoundException {
        String path = "C:\\Users\\95680\\Desktop\\学生.xlsx";
        InputStream inputStream = new FileInputStream(path);

        EasyExcel.read(inputStream, Student.class, new StudentEventListener()).sheet("成都").doRead();
    }
}
