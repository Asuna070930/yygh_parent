package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.exphandler.YyghException;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/5 10:52
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {


    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    HospitalRepository hospitalRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Override
    public void saveSchedule(Map<String, Object> map) {

        //1.签名校验(和上传医院接口的签名校验写法一直)
        String sign = map.get("sign") + "";
        if (StringUtils.isEmpty(sign)) {
            throw new YyghException(20001, "医院端传递签名为空");
        }

        //2、将map转成Schedule对象
        Schedule schedule = JSON.parseObject(JSON.toJSONString(map), Schedule.class);

        //3、从mongodb中查询该排班是存在（根据医院编号+排班编号查询到某一个排班）
        String hoscode = schedule.getHoscode();
        String hosScheduleId = schedule.getHosScheduleId();
        if (StringUtils.isEmpty(hoscode) || StringUtils.isEmpty(hosScheduleId)) {
            throw new YyghException(20001, "医院编号或排班编号为空");
        }
        Schedule scheduleFromMongodb = scheduleRepository.findByHoscodeAndHosScheduleId(hoscode, hosScheduleId);

        //4、判断是否存在
        if (scheduleFromMongodb == null) {
            //5、添加
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            scheduleRepository.save(schedule);
        } else {
            //6、修改
            schedule.setId(scheduleFromMongodb.getId());
            schedule.setCreateTime(scheduleFromMongodb.getCreateTime());
            schedule.setUpdateTime(new Date());
            scheduleRepository.save(schedule);
        }

    }

    @Override
    public Page<Schedule> scheduleList(Map<String, Object> map) {
        //1.签名校验(和上传医院接口的签名校验写法一直)
        String sign = map.get("sign") + "";
        if (StringUtils.isEmpty(sign)) {
            throw new YyghException(20001, "医院端传递签名为空");
        }

        //2、获取医院端的参数（查询某个医院，某个科室下的排班列表）
        String hoscode = map.get("hoscode") + "";//医院编号
        String depcode = map.get("depcode") + "";//科室编号

        int pageNum = Integer.parseInt(map.get("page") + "");//查询第几页，需要转成int类型
        int limit = Integer.parseInt(map.get("limit") + "");//每页多少条，需要转成int类型

        //3、hoscode = ？ （）  如果你是等值查询，就不需要模糊查询的匹配器
//        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);

        Schedule schedule = new Schedule();
        schedule.setHoscode(hoscode);
        schedule.setDepcode(depcode);

        //4、利用department创建example对象
//        Example<Department> example = Example.of(department,exampleMatcher);
        Example<Schedule> example = Example.of(schedule);

        //5、分页 + 排序
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(pageNum - 1, limit, sort);

        //5、调用
        Page<Schedule> all = scheduleRepository.findAll(example, pageable);

        return all;
    }

    @Override
    public void remove(Map<String, Object> map) {
        String hoscode = map.get("hoscode") + "";//医院编号
        String hosScheduleId = map.get("hosScheduleId") + "";//排班编号

        Schedule schedule = scheduleRepository.findByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (schedule != null) {
            String id = schedule.getId();
            scheduleRepository.deleteById(id);
        }
    }

    @Override
    public Map getScheduleRuleVoList(Long page, Long limit, String hoscode, String depcode) {

        //1、条件：针对哪个哪个科室下的排班进行聚合分组统计
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);//.and("workDate").in(日期集合);

        //参数1：聚合对象  参数2：进行聚合统计的数据类型   参数3：聚合后，每一组提取一些数据封装成一个BookingScheduleRuleVo
        //一个BookingScheduleRuleVo对象对应前端的一个小方块（workDate dayOfWeek availableNumber reservedNumber）
        Aggregation agg = Aggregation.newAggregation(Aggregation.match(criteria),//针对哪些排班进行聚合
                Aggregation.group("workDate")//按照排班中的workDate分组聚合
                        //每一组workDate相同的排班，提取一些数据
                        .first("workDate").as("workDate").first("workDate").as("workDateMd").sum("reservedNumber").as("reservedNumber").sum("availableNumber").as("availableNumber").count().as("docCount")//排班个数（医生人数）
                , Aggregation.sort(Sort.Direction.DESC, "workDate")//按照workDate排序
                ,
                //分页
                Aggregation.skip((page - 1) * limit), Aggregation.limit(limit));

        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);

        //当前页的日期列表对应的vo集合
        List<BookingScheduleRuleVo> ruleVoList = aggregate.getMappedResults();

        //计算日期对应的星期（jodaTime组件）
        for (BookingScheduleRuleVo ruleVo : ruleVoList) {
            Date workDate = ruleVo.getWorkDate();//排班日期
            //星期？
            String dayOfWeek = this.getDayOfWeek(workDate);
            ruleVo.setDayOfWeek(dayOfWeek);
        }

        //封装map
        Map map = new HashMap();
        map.put("bookingScheduleRuleVoList", ruleVoList);//当前页的日期列表
        map.put("total", this.calTotal(hoscode, depcode));//总日期个数（总的vo个数）

        return map;
    }

    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate) {
        //2023-07-28 转成Date
        Date date = new DateTime(workDate).toDate();
        List<Schedule> list = scheduleRepository.findByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, date);

        list.forEach(schedule -> {
            this.packSchedule(schedule);//每个schedule对象的param属性，添加三个值（hosname+depname+dayOfWeek）  这三个属性，在后台系统中没有使用，前端挂号网站使用
        });

        return list;
    }

    @Override
    public Map getScheduleRuleVoList2(Long page, Long limit, String hoscode, String depcode) {

        //统计该医院该科室下的排班，都来自于哪些不同的日期
        //每个日期，封装成一个BookingScheduleRuleVo对象
        //并且分页
        //total总日期个数

        //1、计算都有哪些不同的日期
        Set<Date> set = new LinkedHashSet<>();
        List<Schedule> list = scheduleRepository.findByHoscodeAndDepcodeOrderByWorkDateDesc(hoscode, depcode);
        list.forEach(schedule -> {
            Date workDate = schedule.getWorkDate();
            set.add(workDate);
        });

        //所有的ruleVo集合
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();

        //2、根据每一个日期，查询到该日期下的排班列表
        set.forEach(workDate -> {

            //3、该排班列表，统计一些参数，封装成ruleVo
            List<Schedule> scheduleList = scheduleRepository.findByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, workDate);

            BookingScheduleRuleVo ruleVo = new BookingScheduleRuleVo();
            ruleVo.setWorkDate(scheduleList.get(0).getWorkDate());//first(workDate)
            ruleVo.setWorkDateMd(scheduleList.get(0).getWorkDate());
            ruleVo.setDocCount(scheduleList.size());//排班的数量
            ruleVo.setDayOfWeek(this.getDayOfWeek(workDate));

            Map<String, Integer> map = this.calRSum(scheduleList);
            ruleVo.setReservedNumber(map.get("reservedNumber"));
            ruleVo.setAvailableNumber(map.get("availableNumber"));

            bookingScheduleRuleVoList.add(ruleVo);

        });

        //4、分页  list总的集合
        // 1-5  2-5
        long begin = (page - 1) * limit;//0   5
        long end = begin + limit;//5 10

        //越界问题
        if (end > bookingScheduleRuleVoList.size()) {
            end = bookingScheduleRuleVoList.size();
        }

        //分页的日期vo集合
        List<BookingScheduleRuleVo> pageList = new ArrayList<>();
        for (long i = begin; i < end; i++) {
            BookingScheduleRuleVo ruleVo = bookingScheduleRuleVoList.get(Integer.parseInt(i + ""));
            pageList.add(ruleVo);
        }

        Map map = new HashMap();
        map.put("bookingScheduleRuleVoList", pageList);//当前页的日期列表
        map.put("total", bookingScheduleRuleVoList.size());//总日期个数（总的vo个数）

        return map;
    }

    @Override
    public Map getScheduleBookingRule(String hoscode, String depcode, Integer pageNum, Integer pageSize) {

        //1、计算日期范围
        //根据医院编号查询医院对象
        Hospital hospital = hospitalRepository.findByHoscode(hoscode);
        //该医院的预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        //预约周期
        Integer cycle = bookingRule.getCycle();
        //当前医院的放号时间 08:30
        String releaseTime = bookingRule.getReleaseTime();

        //判断如果今天已经开始放号，日期个数=cycle+1
        String s = new DateTime(new Date()).toString("yyyy-MM-dd");
        String date = s + " " + releaseTime;//yyyy-MM-dd HH:mm
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(date);

        if (dateTime.isBeforeNow()) {
            //放号时间在现在之前
            cycle++;
        }

        //计算总页数
        int pages = cycle / pageSize + (cycle % pageSize == 0 ? 0 : 1);

        //根据cycle，创建日期列表（例如：cycle=1，创建11个日期Date对象，每个日期对象不需要时分秒）
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            DateTime dt = new DateTime().plusDays(i);
            String s1 = dt.toString("yyyy-MM-dd");
            Date workDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(s1).toDate();
            dateList.add(workDate);
        }

        //从dateList中截取第一页的7个日期
        int begin = (pageNum - 1) * pageSize;//
        int end = (pageNum - 1) * pageSize + pageSize;

        //判断end是否越界  11
        //end  = 11
        if (end > dateList.size()) {
            end = dateList.size();
        }

        List<Date> pageDateList = new ArrayList<>();
        for (int i = begin; i < end; i++) {
            Date date1 = dateList.get(i);
            pageDateList.add(date1);
        }

        //2、hoscode+depcode+日期范围  条件下的排班进行聚合统计

        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(pageDateList);//10-16号范围内的排班，进行聚合
        Aggregation agg = Aggregation.newAggregation(
                //（1）针对哪些排班
                Aggregation.match(criteria),
                //（2）按照每个排班的workDate字段进行聚合
                Aggregation.group("workDate")
                        //（3）每一组排班提取一些数据，赋值给BookingScheduleRuleVo
                        .first("workDate").as("workDate").first("workDate").as("workDateMd").count().as("docCount").sum("reservedNumber").as("reservedNumber")//每一组reservedNumber求和
                        .sum("availableNumber").as("availableNumber")//每一组availableNumber求和
        );
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);

        //获取聚合后的结果集
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();

        //mappedResults转成map
        //ruleVo中的workDate作为key
        //ruleVo对象本身作为value
        //  Map<Date,BookingScheduleRuleVo> map = new HashMap<>();
        //  for (BookingScheduleRuleVo ruleVo : mappedResults) {
        ////      ruleVo.setStatus(10000);
        //      map.put(ruleVo.getWorkDate(),ruleVo);
        //  }

        Map<Date, BookingScheduleRuleVo> map = mappedResults.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, vo -> vo));

        //3、检查哪些日期没有vo对象，创建一个默认的rulevo对象
        //遍历当前页的日期列表，根据每一个日期判断是否存储ruleVo，如果不存在，创建一个默认的ruleVo
        //10-16  七个日期

        List<BookingScheduleRuleVo> pageBookingScheduleRuleList = new ArrayList<>();

        //对当前页的日期集合遍历
        for (int i = 0; i < pageDateList.size(); i++) {
            Date workDate = pageDateList.get(i);

            BookingScheduleRuleVo ruleVo = map.get(workDate);
            if (ruleVo == null) {
                //当前日期没有ruleVo对象，创建一个默认的ruleVo
                ruleVo = new BookingScheduleRuleVo();
                //为默认的rule赋值
                ruleVo.setWorkDate(workDate);
                ruleVo.setWorkDateMd(workDate);
                ruleVo.setDocCount(-1);//排班数量
                ruleVo.setAvailableNumber(-1);
                ruleVo.setReservedNumber(-1);
            }

            //4、为每一个ruleVo的星期字段赋值
            String dayOfWeek = this.getDayOfWeek(workDate);
            ruleVo.setDayOfWeek(dayOfWeek);

            //5、为每一个ruleVo的status赋值    0：正常 1：即将放号 -1：当天已停止挂号
            //要求：第一页的第一个ruleVo，判断今天是否停止挂号，如果停止挂号，status=-1
            //如果是最后一页的最后一条，status设置成1
            //其余的ruleVo都设置成0

            ruleVo.setStatus(0);

            //第一页的第一条
            if (i == 0 && pageNum == 1) {
                //今天是否停止挂号？
                String stopTime = bookingRule.getStopTime();//11:30

                //yyyy-MM-dd HH:mm
                DateTime stop = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(new DateTime(new Date()).toString("yyyy-MM-dd") + " " + stopTime);
                if (stop.isBeforeNow()) {
                    //今天已经停止挂号
                    ruleVo.setStatus(-1);
                }
            }

            //最后一页最后一条
            if (i == pageDateList.size() - 1 && pageNum == pages) {
                ruleVo.setStatus(1);
            }

            //pageBookingScheduleRuleList存放的就是当前页日期列表对应的所有的ruleVo，并且集合中rule日期是有序的
            pageBookingScheduleRuleList.add(ruleVo);
        }


        //封装返回结果（参考前端页面所需要的数据）
        //this.bookingScheduleList = response.data.bookingScheduleList
        //this.total = response.data.total
        //this.baseMap = response.data.baseMap

        Map result = new HashMap();

        result.put("bookingScheduleList", pageBookingScheduleRuleList);
        result.put("total", cycle);

        Map baseMap = new HashMap();
        baseMap.put("hosname", hospital.getHosname());
        Department department = departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);
        baseMap.put("bigname", department.getBigname());
        baseMap.put("depname", department.getDepname());
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));//2023年07月

        result.put("baseMap", baseMap);//hosname bigname depname workDateString

        return result;
    }

    @Override
    public Map getScheduleBookingRule1(String hoscode, String depcode, Integer pageNum, Integer pageSize) {

        //1、计算日期范围
        //根据医院编号查询医院对象
        Hospital hospital = hospitalRepository.findByHoscode(hoscode);
        //该医院的预约规则
        BookingRule bookingRule = hospital.getBookingRule();

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Date> page = this.getPageDateList(bookingRule, pageSize, pageNum);
        List<Date> pageDateList = page.getRecords();//当前页结果集
        long pages = page.getPages();//总页数
        long total = page.getTotal();//总记录数

        //2、hoscode+depcode+日期范围  条件下的排班进行聚合统计

        List<BookingScheduleRuleVo> mappedResults =this.aggScheduleByCondition(hoscode,depcode,pageDateList);

        //mappedResults转成map
        //ruleVo中的workDate作为key
        //ruleVo对象本身作为value
//        Map<Date,BookingScheduleRuleVo> map = new HashMap<>();
//        for (BookingScheduleRuleVo ruleVo : mappedResults) {
////            ruleVo.setStatus(10000);
//            map.put(ruleVo.getWorkDate(),ruleVo);
//        }

        Map<Date, BookingScheduleRuleVo> map = mappedResults.stream().collect(
                Collectors.toMap(BookingScheduleRuleVo::getWorkDate, vo -> vo
                )
        );



        //3、检查哪些日期没有vo对象，创建一个默认的rulevo对象
        //遍历当前页的日期列表，根据每一个日期判断是否存储ruleVo，如果不存在，创建一个默认的ruleVo
        //10-16  七个日期

        List<BookingScheduleRuleVo> pageBookingScheduleRuleList = new ArrayList<>();

        //对当前页的日期集合遍历
        for (int i = 0; i < pageDateList.size(); i++) {
            Date workDate = pageDateList.get(i);

            BookingScheduleRuleVo ruleVo = map.get(workDate);
            if (ruleVo==null){
                //当前日期没有ruleVo对象，创建一个默认的ruleVo
                ruleVo = new BookingScheduleRuleVo();
                //为默认的rule赋值
                ruleVo.setWorkDate(workDate);
                ruleVo.setWorkDateMd(workDate);
                ruleVo.setDocCount(-1);//排班数量
                ruleVo.setAvailableNumber(-1);
                ruleVo.setReservedNumber(-1);
            }
            //为每一个rulevo赋值
            this.setStatusAndDayOfWeek(ruleVo,i,pageNum,bookingRule,pages,pageDateList);





            //pageBookingScheduleRuleList存放的就是当前页日期列表对应的所有的ruleVo，并且集合中rule日期是有序的
            pageBookingScheduleRuleList.add(ruleVo);



        }



        //封装返回结果（参考前端页面所需要的数据）
//        this.bookingScheduleList = response.data.bookingScheduleList
//        this.total = response.data.total
//        this.baseMap = response.data.baseMap

        Map result = new HashMap();

        result.put("bookingScheduleList",pageBookingScheduleRuleList);
        result.put("total",total);

        Map baseMap = new HashMap();
        baseMap.put("hosname",hospital.getHosname());
        Department department = departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);
        baseMap.put("bigname",department.getBigname());
        baseMap.put("depname",department.getDepname());
        baseMap.put("workDateString",new DateTime().toString("yyyy年MM月"));//2023年07月

        result.put("baseMap",  baseMap);//hosname bigname depname workDateString

        return result;

    }

    @Override
    public Schedule getSchedule(String scheduleId) {
        Optional<Schedule> byId = scheduleRepository.findById(scheduleId);
        Schedule schedule = byId.get();
        this.packSchedule(schedule);
        return schedule;
    }

    @Override
    public void updateSchedule(OrderMqVo orderMqVo) {
        //将消息中的两个num和排班id取出，根据排班id更新两个num
        String scheduleId = orderMqVo.getScheduleId();//mongodb中的排班id
        Optional<Schedule> byId = scheduleRepository.findById(scheduleId);
        Schedule schedule = byId.get();

        //医院端返回的最新的两个num
        Integer reservedNumber = orderMqVo.getReservedNumber();
        Integer availableNumber = orderMqVo.getAvailableNumber();

        schedule.setAvailableNumber(availableNumber);
        schedule.setReservedNumber(reservedNumber);
        schedule.setUpdateTime(new Date());

        scheduleRepository.save(schedule);
    }


    private void setStatusAndDayOfWeek(BookingScheduleRuleVo ruleVo,int i,int pageNum,BookingRule bookingRule,long pages,List<Date>pageDateList) {
        //4、为每一个ruleVo的星期字段赋值
        String dayOfWeek = this.getDayOfWeek(ruleVo.getWorkDate());
        ruleVo.setDayOfWeek(dayOfWeek);

        //5、为每一个ruleVo的status赋值    0：正常 1：即将放号 -1：当天已停止挂号
        //要求：第一页的第一个ruleVo，判断今天是否停止挂号，如果停止挂号，status=-1
        //如果是最后一页的最后一条，status设置成1
        //其余的ruleVo都设置成0

        ruleVo.setStatus(0);

        //第一页的第一条
        if (i==0 && pageNum == 1){
            //今天是否停止挂号？
            String stopTime = bookingRule.getStopTime();//11:30

            //yyyy-MM-dd HH:mm
            // DateTime stop = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(new DateTime(new Date()).toString("yyyy-MM-dd") + " " + stopTime);
            DateTime dateTime = this.getDateTime(new Date(), stopTime);
            if(dateTime.isBeforeNow()){
                //今天已经停止挂号
                ruleVo.setStatus(-1);
            }
        }
        //最后一页最后一条
        if(i == pageDateList.size()-1 && pageNum == pages  ){
            ruleVo.setStatus(1);
        }
    }

    /**
     * @param date 日期对象
     * @param time 08:30   11:30
     * @return yyyy-MM-dd HH:mm 格式的 DateTime
     */
    private  DateTime getDateTime(Date date,String time){
        String s =new DateTime(date).toString("yyyy-MM-dd")+" "+time;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(s);
        return dateTime;
    }

    private List<BookingScheduleRuleVo> aggScheduleByCondition(String hoscode,String depcode,List<Date> pageDateList) {
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(pageDateList);//10-16号范围内的排班，进行聚合
        Aggregation agg = Aggregation.newAggregation(
                //（1）针对哪些排班
                Aggregation.match(criteria),
                //（2）按照每个排班的workDate字段进行聚合
                Aggregation.group("workDate")
                        //（3）每一组排班提取一些数据，赋值给BookingScheduleRuleVo
                        .first("workDate").as("workDate")
                        .first("workDate").as("workDateMd")
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")//每一组reservedNumber求和
                        .sum("availableNumber").as("availableNumber")//每一组availableNumber求和
        );
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);

        //获取聚合后的结果集
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();
        return mappedResults;
    }

    private com.baomidou.mybatisplus.extension.plugins.pagination.Page<Date> getPageDateList(BookingRule bookingRule, Integer pageSize, Integer pageNum) {
        //预约周期
        Integer cycle = bookingRule.getCycle();
        //当前医院的放号时间 08:30
        String releaseTime = bookingRule.getReleaseTime();

        //判断如果今天已经开始放号，日期个数=cycle+1
        String s = new DateTime(new Date()).toString("yyyy-MM-dd");
        String date = s +" " + releaseTime;//yyyy-MM-dd HH:mm
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(date);

        if(dateTime.isBeforeNow()){
            //放号时间在现在之前
            cycle++;
        }

        //计算总页数
        int pages = cycle / pageSize  + (cycle%pageSize==0?0:1);



        //根据cycle，创建日期列表（例如：cycle=1，创建11个日期Date对象，每个日期对象不需要时分秒）
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            DateTime dt = new DateTime().plusDays(i);
            String s1 = dt.toString("yyyy-MM-dd");
            Date workDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(s1).toDate();
            dateList.add(workDate);
        }



        //从dateList中截取第一页的7个日期
        int begin = (pageNum-1)*pageSize;//
        int end = (pageNum-1)*pageSize + pageSize ;

        //判断end是否越界  11
        //end  = 11
        if(end>dateList.size()){
            end = dateList.size();
        }

        List<Date> pageDateList = new ArrayList<>();
        for (int i = begin; i < end; i++) {
            Date date1 = dateList.get(i);
            pageDateList.add(date1);
        }
        //借用mybatisplus中的page类  封装page对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Date> datePage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum,pageSize,cycle);
        datePage.setRecords(pageDateList);//当前页结果集
        return datePage;

    }



    private Map<String, Integer> calRSum(List<Schedule> scheduleList) {
        Integer reservedNumber = 0;
        Integer availableNumber = 0;

        Map<String, Integer> map = new HashMap<>();

        for (Schedule schedule : scheduleList) {
            reservedNumber += schedule.getReservedNumber();
            availableNumber += schedule.getAvailableNumber();
        }

        map.put("reservedNumber", reservedNumber);
        map.put("availableNumber", availableNumber);

        return map;
    }


    private void packSchedule(Schedule schedule) {
        String hoscode = schedule.getHoscode();
        String depcode = schedule.getDepcode();

        Hospital hospital = hospitalRepository.findByHoscode(hoscode);
        String hosname = hospital.getHosname();//当前排班对应的医院名称

        Department department = departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);
        String depname = department.getDepname();//当前排班对应的小科室名称

        Date workDate = schedule.getWorkDate();
        String dayOfWeek = this.getDayOfWeek(workDate);//当前排班对应的星期

        schedule.getParam().put("hosname", hosname);
        schedule.getParam().put("depname", depname);
        schedule.getParam().put("dayOfWeek", dayOfWeek);

    }

    private Integer calTotal(String hoscode, String depcode) {
        //1、条件：针对哪个哪个科室下的排班进行聚合分组统计
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

        //参数1：聚合对象  参数2：进行聚合统计的数据类型   参数3：聚合后，每一组提取一些数据封装成一个BookingScheduleRuleVo
        //一个BookingScheduleRuleVo对象对应前端的一个小方块（workDate dayOfWeek availableNumber reservedNumber）
        Aggregation agg = Aggregation.newAggregation(Aggregation.match(criteria),//针对哪些排班进行聚合
                Aggregation.group("workDate")//按照排班中的workDate分组聚合
                //每一组workDate相同的排班，提取一些数据
//                        .first("workDate").as("workDate")
//                        .first("workDate").as("workDateMd")
//                        .sum("reservedNumber").as("reservedNumber")
//                        .sum("availableNumber").as("availableNumber")
//                        .count().as("docCount")//排班个数（医生人数）
                , Aggregation.sort(Sort.Direction.DESC, "workDate")//按照workDate排序
//                ,
                //分页
//                Aggregation.skip( (page-1)*limit ),
//                Aggregation.limit(limit)
        );

        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);


        //当前页的日期列表对应的vo集合
        List<BookingScheduleRuleVo> ruleVoList = aggregate.getMappedResults();
        return ruleVoList.size();
    }

    //Date日期对应的星期
    private String getDayOfWeek(Date workDate) {
        //使用jodaTime组件，计算Date对应的星期
        DateTime dateTime = new DateTime(workDate);
        int dayOfWeek = dateTime.getDayOfWeek();//1-7（1：周一）  使用switch也行
        List<String> strings = Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日");
        return strings.get(dayOfWeek - 1);
    }

    public static void main(String[] args) {
        //2023-07-28 转成Date
        String workDate = "2023-07-28";
        Date date = new DateTime(workDate).toDate();
        System.out.println(date);
    }
}
