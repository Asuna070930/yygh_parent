package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.exphandler.YyghException;
import com.atguigu.common.utils.MD5;
import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/5 10:48
 */
@Service
public class HospitalServiceImpl implements HospitalService {
    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    HospitalSetService hospitalSetService;

    @Autowired
    DictFeignClient dictFeignClient;


    /*
     *    1、医院端可以重复调用该接口，实现上传医院信息或者修改医院信息
     *    2、尚医通端开发的8个接口中都需要对医院端传递过来的sign签名进行校验，并且医院端传递的sign是经过了md5加密的
     *        如何校验？从平台端的数据库中查询到该医院正确的签名，和医院端传递的签名进行比较（这个过程就是验签，为了保证安全性）
     *    3、医院端传递的logoData表示医院的图片，对应类型是字符串；该接口中需要将该字符串中的所有的空格替换成+     “ ” = 》 “+”
     *    4、该接口中无论是添加还是修改医院，status状态默认设置成1
     *    5、8个接口都需要校验该医院是否开通了权限, 不满足要求，则抛出异常
     *    6、对必要参数的非空校验，不满足要求，则抛出异常
     * */
    @Override
    public void saveHospital(Map<String, Object> map) {

        //1、从parameterMap中获取hoscode
        String hoscode = map.get("hoscode") + "";
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(20001, "医院编号为空");
        }

        //2、根据hoscode去查询医院设置，进而找到签名key
        HospitalSet hospitalSet = hospitalSetService.getByHoscode(hoscode);
        if (hospitalSet == null) {
            throw new YyghException(20001, "未开通医院设置");
        }

        //3、获取签名（未加密）
        String signKey = hospitalSet.getSignKey();
        if (StringUtils.isEmpty(signKey)) {
            throw new YyghException(20001, "医院设置的签名为空");
        }

        //4、校验签名，所有的接口执行业务逻辑之前都要先校验签名
        String sign = map.get("sign") + "";//（加密的）
        if (StringUtils.isEmpty(sign)) {
            throw new YyghException(20001, "医院端传递的签名为空");
        }

        //5、校验
        if (!MD5.encrypt(signKey).equals(sign)) {
            throw new YyghException(20001, "验签失败");
        }

        //6、map转成hospital对象
        String jsonString = JSON.toJSONString(map);
        Hospital hospital = JSON.parseObject(jsonString, Hospital.class);

        //7、处理status 和 logoData
        hospital.setStatus(1);//status默认值设置1
        String logoData = hospital.getLogoData().replaceAll(" ", "+");
        hospital.setLogoData(logoData);


        //8、判断该医院是否存在，去mongodb根据hoscode查询Hospital对象
        Hospital hospitalFromMongodb = hospitalRepository.findByHoscode(hoscode);

        if (hospitalFromMongodb == null) {
            //9、mongodb中不存在该医院，添加医院
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        } else {
            //10、医院已经存在，修改医院
            hospital.setId(hospitalFromMongodb.getId());
            hospital.setCreateTime(hospitalFromMongodb.getCreateTime());//使用原来的创建时间
            hospital.setUpdateTime(new Date());//最后更新时间
            hospitalRepository.save(hospital);
        }

    }

    @Override
    public Hospital show(Map<String, Object> map) {
        //1、验签（省略）
        String sign = map.get("sign") + "";
        if (StringUtils.isEmpty(sign)) {
            throw new YyghException(20001, "医院端传递的签名为空");
        }
        //2、根据hoscode查询某个医院对象
        String hoscode = map.get("hoscode") + "";
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(20001, "医院编号为空");
        }

        //3、根据hoscode查询医院对象
        Hospital hospital = hospitalRepository.findByHoscode(hoscode);
        return hospital;
    }

    @Override
    public Page<Hospital> pageList(HospitalQueryVo hospitalQueryVo, Integer pageNum, Integer pageSize) {
        //查询医院列表，科室列表，排班列表  写法本质是一样的

        //1、从vo中解析查询条件
        String hosname = hospitalQueryVo.getHosname();//查询表单中，输入的字符串
        String provinceCode = hospitalQueryVo.getProvinceCode();//选中的省份
        String cityCode = hospitalQueryVo.getCityCode();//选中的城市


        //2、封装成Hospital
        Hospital hospital = new Hospital();
        hospital.setHosname(hosname);
        hospital.setProvinceCode(provinceCode);
        hospital.setCityCode(cityCode);


        //
        hospital.setStatus(hospitalQueryVo.getStatus());

        //3、需要模糊查询
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);


        //4、example对象
        Example<Hospital> example = Example.of(hospital, matcher);


        //5、分页对象
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));


        //6、调用Repository中的findAll方法
        Page<Hospital> all = hospitalRepository.findAll(example, pageRequest);


        //7、将查询到的当前页结果集中的每一个hospital的param属性额外存入两个属性  hostypeString + fullAddress

        all.getContent().forEach(item -> {
            this.packHospital(item);//处理param的额外属性
        });

        return all;
    }

    @Override
    public void updateStatus(String id, Integer status) {
        //根据id修改status
        Optional<Hospital> byId = hospitalRepository.findById(id);
        Hospital hospital = byId.get();
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        hospitalRepository.save(hospital);
    }

    @Override
    public Map showDetail(String id) {
//        map = hospital + bookingRule

        Hospital hospital = hospitalRepository.findById(id).get();
        this.packHospital(hospital);//param中添加两个属性

        Map map = new HashMap();
        map.put("hospital", hospital);
        map.put("bookingRule", hospital.getBookingRule());

        return map;
    }

    @Override
    public List<Hospital> findByHosname(String keyword) {
//        List<Hospital> list = hospitalRepository.findByHosnameLike(keyword);
        Hospital hospital = new Hospital();
        hospital.setHosname(keyword);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase(true).withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<Hospital> example = Example.of(hospital, matcher);

        List<Hospital> list = hospitalRepository.findAll(example);
        return list;
    }

    @Override
    public Map findByHoscode(String hoscode) {
        Hospital hospital = hospitalRepository.findByHoscode(hoscode);
        this.packHospital(hospital);//获取医院等级
        BookingRule bookingRule = hospital.getBookingRule();

        Map map = new HashMap();
        map.put("hospital", hospital);
        map.put("bookingRule", bookingRule);

        hospital.setBookingRule(null);
        return map;
    }

    @Override
    public Hospital getHospital(String hoscode) {
        return hospitalRepository.findByHoscode(hoscode);
    }

    /**
     * 每一个医院，都需要调用数据字典服务，获取医院等级名称，省市区名称
     *
     * @param item
     */
    private void packHospital(Hospital item) {

        //数据字典表中的
        //医院等级的value（不唯一的需要使用两个参数的getName方法）
        String hostypeValue = item.getHostype();

        //省市区的value
        String provinceCode = item.getProvinceCode();
        String cityCode = item.getCityCode();
        String districtCode = item.getDistrictCode();

        //医院等级名称，注意：两个参数的顺序别写反了
        String hostypeString = dictFeignClient.getName(DictEnum.HOSTYPE.getDictCode(), hostypeValue);

        //省市区名称
        String provinceString = dictFeignClient.getName(provinceCode);
        String cityString = dictFeignClient.getName(cityCode);
        String districtString = dictFeignClient.getName(districtCode);

        String fullAddress = provinceString + cityString + districtString + item.getAddress();

        item.getParam().put("hostypeString", hostypeString);
        item.getParam().put("fullAddress", fullAddress);

    }
}
