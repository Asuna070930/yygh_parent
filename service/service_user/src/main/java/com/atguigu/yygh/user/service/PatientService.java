package com.atguigu.yygh.user.service;

import com.atguigu.yygh.model.user.Patient;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/13 8:54
 */
public interface PatientService extends IService<Patient> {
    List<Patient> findAll(Long userId);

    Patient findOne(Long patientId);

    Integer getPatientCount(Long userId);

    Integer getCountByYear(Long userId);

    Integer getContactsPhoneCount(String contactsPhone);
}

