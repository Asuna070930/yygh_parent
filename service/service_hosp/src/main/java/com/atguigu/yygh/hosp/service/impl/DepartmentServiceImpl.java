package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.exphandler.YyghException;
import com.atguigu.common.utils.MD5;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/5 10:51
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    HospitalSetService hospitalSetService;

    @Autowired
    DepartmentRepository departmentRepository;

    @Override
    public void saveDepartment(Map<String, Object> map) {
        //1.签名校验(和上传医院接口的签名校验写法一直)
        String sign = map.get("sign") + "";
        if (StringUtils.isEmpty(sign)) {
            throw new YyghException(20001, "医院端传递签名为空");
        }

        //2.获取医院端传递的医院编号
        String hoscode = map.get("hoscode") + "";
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(20001, "医院端传递的医院编号为空");
        }

        //3.根据hoscode查询该医院的签名
        HospitalSet hospitalSet = hospitalSetService.getByHoscode(hoscode);
        if (hospitalSet == null) {
            throw new YyghException(20001, "该医院未开通权限");
        }

        //4.从hospitalSet取出签名
        String signKey = hospitalSet.getSignKey();
        if (StringUtils.isEmpty(signKey)) {
            throw new YyghException(20001, "医院设置中的签名为空");
        }

        //5.开始校验
        if (!MD5.encrypt(signKey).equals(sign)) {
            throw new YyghException(20001, "验签失败");
        }

        //6.医院端传递过来的参数类型时map,转成department类型
        Department department = JSON.parseObject(JSON.toJSONString(map), Department.class);

        //7.查询到该科室
        Department departmentFromMongodb = departmentRepository.findByHoscodeAndDepcode(department.getHoscode(), department.getDepcode());

        if (departmentFromMongodb != null) {
            //8.存在执行修改
            department.setCreateTime(departmentFromMongodb.getCreateTime());
            department.setUpdateTime(new Date());
            department.setId(department.getId());
            departmentRepository.save(department);
        } else {
            //9.不存在,执行添加
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            departmentRepository.save(department);
        }
    }

    @Override
    public Page<Department> departmentList(Map<String, Object> map) {

        //1、验签（省略）
        String sign = map.get("sign") + "";
        if (StringUtils.isEmpty(sign)) {
            throw new YyghException(20001, "医院端传递签名为空");
        }
        //2、获取医院端的参数
        String hoscode = map.get("hoscode") + "";//医院编号

        int pageNum = Integer.parseInt(map.get("page") + "");//查询第几页，需要转成int类型
        int limit = Integer.parseInt(map.get("limit") + "");//每页多少条，需要转成int类型


        //3、hoscode = ？ （）  如果你是等值查询，就不需要模糊查询的匹配器
//        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);

        Department department = new Department();
        department.setHoscode(hoscode);


        //4、利用department创建example对象
        //Example<Department> example = Example.of(department,exampleMatcher);
        Example<Department> example = Example.of(department);


        //5、分页 + 排序
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(pageNum - 1, limit, sort);

        //5、调用
        Page<Department> all = departmentRepository.findAll(example, pageable);

        return all;
    }


    //删除科室
    @Override
    public void remove(Map<String, Object> map) {

        String hoscode = map.get("hoscode") + "";
        String depcode = map.get("depcode") + "";

        //删除科室
//        departmentRepository.removeByHoscodeAndDepcode(hoscode,depcode);

        Department department = departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);
        if (department != null) {
            String id = department.getId();//科室的id
            departmentRepository.deleteById(id);
        }
    }

    @Override
    public List<DepartmentVo> departmentVoList(String hoscode) {

        List<DepartmentVo> list = new ArrayList<>();

        //1、根据hoscode查询该医院所有的小科室（没有大科室集合）
        List<Department> departmentList = departmentRepository.findByHoscode(hoscode);

        //2、针对所有的小科室，使用java8 的groupBy 按照每一个小科室的bigcode进行分组，分了多少组就说明有多少不同的大科室
        Map<String, List<Department>> collect = departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));


        //3、每一个大科室封装成一个DepartmentVo对象，三个属性：depcode大科室编号 depname大科室名称 children小科室集合（注意：泛型也是DepartmentVo，只是小科室的children为空即可）
        for (Map.Entry<String, List<Department>> entry : collect.entrySet()) {
            String bigcode = entry.getKey();//当前大科室编号
            List<Department> value = entry.getValue();//bigcode相同的一组小科室集合，bigname也是相同的

            //封装大科室对象
            DepartmentVo big = new DepartmentVo();
            big.setDepcode(bigcode);//大科室编号
            big.setDepname(value.size() > 0 ? value.get(0).getBigname() : "暂无名称");//大科室名称
            big.setChildren(this.transfer(value));//bigcode相同的一组小科室集合，泛型DepartmentVo，只是children为空

            list.add(big);
        }

        return list;
    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {
        Department department = departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);

        return department;
    }

    private List<DepartmentVo> transfer(List<Department> value) {
        List<DepartmentVo> smallList = new ArrayList<>();
        //参数value是小科室集成
        for (Department department : value) {
            //重新封装小科室对象
            DepartmentVo departmentVo = new DepartmentVo();
            departmentVo.setDepcode(department.getDepcode());//小科室编号
            departmentVo.setDepname(department.getDepname());//小科室名称
//            departmentVo.setChildren(); 小科室的children为空

            smallList.add(departmentVo);
        }
        return smallList;
    }
}
