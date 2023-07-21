package com.atguigu.cmn.controller;

import com.atguigu.cmn.service.DictService;
import com.atguigu.common.result.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/3 9:59
 */
@RestController
@RequestMapping("/admin/cmn")
public class DictController {
    @Autowired
     DictService dictService;

    @GetMapping("findAll")
    public R findAll() {
        return R.ok().data("list", dictService.list());
    }
}
