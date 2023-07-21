package com.atguigu.yygh.hosp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("t_student")//mg中的集合名称
public class Student {

    @Id //用来和mg中的文档中的_id（ObjectId，对应java中的String） 字段映射
    private String id;

    @Field(value = "stu_name")//name属性映射到文档中的字段
    //@Indexed(unique = true)建立唯一索引
    //@Indexed 建立普通索引
    private String name;

    //    @Field 不适用该注解，说明类中的属性名和文档中的字段名是一致的
    private String address;

    private Integer age;


}
