package com.atguigu.yygh.hosp;

import com.atguigu.yygh.hosp.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/4 20:57
 */
@SpringBootTest
public class MongoTest {

    //添加了mg的启动器（starter），springboot完成的自动配置，直接使用MongoTemplate对象操作mg
    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    ScheduleService scheduleService;


    @Test
    public void test13() {
        String hoscode = "10000";
        String depcode = "200040182";
        Map map1 = scheduleService.getScheduleRuleVoList(1L, 5L, hoscode, depcode);
        Map map2 = scheduleService.getScheduleRuleVoList2(1L, 5L, hoscode, depcode);

        System.out.println(map1);
        System.out.println(map2);
    }


    @Test
    public void test1() {
        //主键重复
        //org.springframework.dao.DuplicateKeyException: E11000 duplicate key error collection: yygh.t_student index: _id_ dup key: { _id: "123qwe" }
        Student student = new Student();
//        student.setId("123qwe"); 该id值在数据库中存在，insert方法报错；id值不存在，使用自定义的id值作为主键；不赋值id，mg中自动生成一个objectid值作为主键
        student.setName("张三");
        student.setAddress("北京");
        mongoTemplate.insert(student);
    }


    //save可以添加和修改
    //对象中的id没有赋值，或者 赋值了但是mg中不存在这个id==》添加
    //id有值，并且在mg中存在，执行更新操作
    @Test
    public void test2() {
        Student student = new Student();

//        student.setId("12345");

        student.setName("李四");
        student.setAddress("上海");
        mongoTemplate.save(student);
    }

    @Test
    public void test3() {
//        mongoTemplate.remove(Student.class);//删除集合中所有的数据

        //用于拼接条件（删除，查询，更新 涉及到的条件）

        Query query = new Query();
        query.addCriteria(Criteria.where("address").is("上海")); //address = 上海

        mongoTemplate.remove(query, Student.class);
    }

    @Test
    public void test4() {
        //修改
//        mongoTemplate.save()

        // _id = 123qwe  的用户 姓名改成王五

        Query query = new Query(Criteria.where("_id").is("123qwe"));
        Update update = new Update();
//        update.set("name","王五");//name： 类中的属性名
        update.set("stu_name", "郑六");//stu_name 文档中的字段名；  两种写法都可以

        mongoTemplate.updateMulti(query, update, Student.class);
    }


    @Test
    public void test5() {
        //查询
        //Student student = mongoTemplate.findById("123qwe", Student.class);
        //System.out.println(student);

        //查询所有
        //List<Student> list = mongoTemplate.findAll(Student.class);

        //条件查询 address = 北京 and  name  = 张三
        // =  is
        //
        //Criteria.where("address").is();//=
        //Criteria.where("address").gt();// >
        //Criteria.where("address").in();// key in ( 1,2,3 )

        //Query query = new Query(Criteria.where("address").is("北京").and("name").is("张三"));
        Query query = new Query(Criteria.where("address").is("北京"));
        //query.with(Sort.by(Sort.Direction.DESC,"age"));//排序  age降序
        query.with(PageRequest.of(1, 2));//分页, 0 : 表示第一页

        //分页的另一写法

        //query.skip();//跳过多少条
        //query.limit();//返回多少条

        List<Student> list = mongoTemplate.find(query, Student.class);
        for (Student student : list) {
            System.out.println(student);
        }
    }

    //mongoTemplate 实现聚合查询
    //
    @Test
    public void test6() {
        for (int i = 0; i < 10; i++) {
            Student student = new Student();
            student.setName(UUID.randomUUID().toString().replace("-", "").substring(0, 8));
            student.setAddress("北京");
            student.setAge(20 + i);

            mongoTemplate.save(student);
        }
    }

    //扩展练习
    @Test
    public void test7() {

        List<Result> resultList = new ArrayList<>();

        //1、查询所有不同的address
        List<Student> list = mongoTemplate.findAll(Student.class);
        Set<String> addressSet = new HashSet<>();
        list.forEach(student -> {
            String address = student.getAddress();
            addressSet.add(address);
        });

        //2、根据不同的address找到对应的学生集合
        addressSet.forEach(address -> {
            Query query = new Query();
            query.addCriteria(Criteria.where("address").is(address));
            List<Student> studentList = mongoTemplate.find(query, Student.class);

            Result result = new Result();

            //3、计算集合的长度，也就是学生的数量，赋值给stuCount
            int size = studentList.size();
            result.setStuCount(size);

            //4、计算这一组学生的age的总和，赋值给ageSum属性
            int sumAge = 0;
            for (Student student : studentList) {
                Integer age = student.getAge();
                sumAge += age;
            }
            result.setAgeSum(sumAge);//年龄总和

            //5、提取到一个地址address赋值给Result中的address属性
//            result.setAddress(address);
            result.setAddress(studentList.get(0).getAddress());


            resultList.add(result);
            System.out.println(result);

        });


//        System.out.println(resultList);


    }


    @Test
    public void test8() {

//        List<Student> list = mongoTemplate.findAll(Student.class);
        Query query = new Query(Criteria.where("age").gt(25));
        List<Student> list = mongoTemplate.find(query, Student.class);

        //java8中的api
        //list集合，按照集合中元素的address属性进行分组，得到map；该map中有几组k-v，取决于分了多少组
        //key： address
        //value： address相同的一组学生集合
        Map<String, List<Student>> collect = list.stream().collect(Collectors.groupingBy(Student::getAddress));

//        System.out.println(collect);
        for (Map.Entry<String, List<Student>> entry : collect.entrySet()) {
            String key = entry.getKey();//address
            List<Student> value = entry.getValue();//address相同的一组学生集合


            Result result = new Result();
//            result.setAddress(key);
            result.setAddress(value.get(0).getAddress());
            result.setStuCount(value.size());
//            result.setAgeSum();

        }
    }


    //mongodb中的聚合
    @Test
    public void test9() {

//        Criteria criteria = Criteria.where("age").gt(18);

        //参数1：聚合规则；  指定按照哪一个字段进行聚合，聚合后每一组提取哪些数据，赋值给Result中的哪些属性
        Aggregation agg = Aggregation.newAggregation(
                //（1） 针对Student集合中哪些数据进行聚合
//                Aggregation.match(criteria), 不写match，就想当针对该集合下所有的文档进行聚合
                //（2）指定按照哪一个字段进行分组聚合
                Aggregation.group("address")
                        .first("address").as("address") //（3）每一组需要提取的数据，以及赋值给Result中的那个属性
                        .sum("age").as("ageSum") //每一组学生集合，对age求和，赋值给Result中的ageSum属性
                        .count().as("stuCount")
                ,
                //排序
                Aggregation.sort(Sort.Direction.ASC, "ageSum"),
                //分页
                Aggregation.skip(2L),
                Aggregation.limit(3)
        );

        //参数2：被聚合的数据类型   参数3：聚合后产出的结果类型
        AggregationResults<Result> aggregate = mongoTemplate.aggregate(agg, Student.class, Result.class);


        //从aggregate中调用get方法
        List<Result> list = aggregate.getMappedResults();

        for (Result result : list) {
            System.out.println(result);
        }

    }


//    ----------MongoRepository接口---------

    @Test
    public void test10() {
        //1、添加和修改
        //和mongoTemplate中的insert和save的特点是一样的
//        studentRepository.insert(); 添加
//        studentRepository.save(); 添加、修改

        //2、删除
//        studentRepository.deleteById(xxxx);
//        studentRepository.deleteAll();

        //3、查询
//        Optional<Student> byId = studentRepository.findById(xx);
//        Student student = byId.get();

        //** 条件+分页查询+排序
        //address = "重庆"
        Student student = new Student();
        student.setAddress("重庆");


        Example<Student> example = Example.of(student);

        //条件查询 ， 没有分页
//        List<Student> all = studentRepository.findAll(example);

        int pageNum = 1;//查询第一页
        int pageSize = 3;

        Sort sort = Sort.by(Sort.Direction.ASC, "age");//按照age升序排序
        //PageRequest是Pageable接口的实现类
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);//注意：mongodb中的分页，0：表示第一页

        //调用接口中的条件的分页查询并且排序
        Page<Student> page = studentRepository.findAll(example, pageable);

        //当前页的结果集（page对象中的content属性）
        List<Student> content = page.getContent();
        // page对象中的totalElements属性，总记录数
        long totalElements = page.getTotalElements();

        System.out.println(content);
        System.out.println(totalElements);


    }


    //条件（模糊）+分页+排序
    @Test
    public void test11() {
        int pageNum = 1; //查询第1页
        int pageSize = 3;//每页3条

        //查询条件（模糊查询） address like '%庆%' and  name like ?

        //1、将查询条件封装成实体对象
        Student student = new Student();
        student.setAddress("庆");
//        student.setName("庆");

        //2、创建模糊查询条件匹配器--字符串包含
        ExampleMatcher matcher = ExampleMatcher
                .matching()
                .withIgnoreCase(true)//忽略大小写
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);//对于字符串条件值，采用包含查询（ like '%string%' ） 字符串包含
//                .withStringMatcher(ExampleMatcher.StringMatcher.ENDING);//对于字符串条件值，采用包含查询（ like '%string' ） 以字符串结尾
//                .withStringMatcher(ExampleMatcher.StringMatcher.STARTING);//对于字符串条件值，采用包含查询（ like 'string%' ） 以字符串开头

        //3、利用matcher + student 封装example对象
        Example<Student> example = Example.of(student, matcher);

        //4、创建排序对象 年龄降序排序
        Sort sort = Sort.by(Sort.Direction.DESC, "age");

        //5、创建分页对象
        PageRequest of = PageRequest.of(pageNum - 1, pageSize, sort);//注意：0表示第一页

        //6、调用接口方法
        //返回值Page： org.springframework.data.domain.Page
        Page<Student> page = studentRepository.findAll(example, of);

        //7、从page中解析数据
        List<Student> content = page.getContent();//当前页结果集
        long totalElements = page.getTotalElements();//总记录数

        for (Student student1 : content) {
            System.out.println(student1);
        }
        System.out.println(totalElements);

    }

    @Test
    public void test12() {
        studentRepository.removeByAgeGreaterThan(25);
    }

}
