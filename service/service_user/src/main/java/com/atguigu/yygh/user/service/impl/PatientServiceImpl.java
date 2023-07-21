package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.mapper.PatientMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/13 8:55
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {


    @Autowired
    DictFeignClient dictFeignClient;

    @Override
    public List<Patient> findAll(Long userId) {
        QueryWrapper<Patient> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<Patient> patientList = baseMapper.selectList(queryWrapper);

        patientList.forEach(patient -> {
            this.packPatient(patient);//每个就诊人的param中添加一些额外的属性值
        });

        return patientList;
    }

    @Override
    public Patient findOne(Long patientId) {
        Patient patient = baseMapper.selectById(patientId);
        this.packPatient(patient);
        return patient;
    }

    @Override
    public Integer getPatientCount(Long userId) {
        QueryWrapper<Patient> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        return baseMapper.selectCount(queryWrapper);
    }

    @Override
    public Integer getCountByYear(Long userId) {

        String yyyy = new DateTime().toString("yyyy");

        QueryWrapper<Patient> queryWrapper = new QueryWrapper<>();
        //1、当前用户在指定年份未被删除的就诊人
        queryWrapper.eq("user_id",userId);
        queryWrapper.eq("YEAR(create_time)",yyyy);

        //2、当前用户在指定年份已被删除的就诊人
        queryWrapper.last("or ( is_deleted = 1 and  user_id = "+userId+" and YEAR(create_time) = '"+yyyy+"' )" );

        Integer integer = baseMapper.selectCount(queryWrapper);

        return integer;
    }

    @Override
    public Integer getContactsPhoneCount(String contactsPhone) {
        QueryWrapper<Patient> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("contacts_phone",contactsPhone);
        return baseMapper.selectCount(queryWrapper);
    }

    private void packPatient(Patient patient) {

        //省市区的value，和证件类型的value在dict表中都是唯一的
        String certificatesType = patient.getCertificatesType();//就诊人的证件类型编号（dict表中的value）
        String provinceCode = patient.getProvinceCode();
        String cityCode = patient.getCityCode();
        String districtCode = patient.getDistrictCode();

        String s4 = "";
        if(!StringUtils.isEmpty(certificatesType)){
            s4 = dictFeignClient.getName(certificatesType);
        }


        String s1 = "";
        String s2 = "";
        String s3 = "";
        if(!StringUtils.isEmpty(provinceCode)){
            s1 = dictFeignClient.getName(provinceCode);
        }
        if(!StringUtils.isEmpty(cityCode)){
            s2 = dictFeignClient.getName(cityCode);
        }
        if(!StringUtils.isEmpty(districtCode)){
            s3 = dictFeignClient.getName(districtCode);
        }


        patient.getParam().put("certificatesTypeString",s4);//就诊人证件类型名称
        patient.getParam().put("provinceString",s1);//省份名称
        patient.getParam().put("cityString",s2);//城市名称
        patient.getParam().put("districtString",s3);//区名称
        patient.getParam().put("fullAddress",s1+s2+s3+patient.getAddress());//完整地址
    }
}
