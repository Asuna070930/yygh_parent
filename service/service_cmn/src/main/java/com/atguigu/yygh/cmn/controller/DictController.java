package com.atguigu.yygh.cmn.controller;

import com.atguigu.common.result.R;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/3 9:59
 */
@Api(description = "数据字典接口文档")
//@CrossOrigin
@RestController
@RequestMapping("/admin/cmn")
public class DictController {

    @Autowired
    private DictService dictService;

    //Hostype - 查询医院等级列表
    //Province - 省份列表
    //CertificatesType - 证件类型列表
    @GetMapping("findByDictCode/{dictCode}")
    public R findByDictCode(@PathVariable String dictCode){
        List<Dict> list = dictService.findByDictCode(dictCode);
        return R.ok().data("list",list);
    }


    /**
     * 根据value查询省市区的名称，参数只有一个
     * @param value
     * @return
     */
    @GetMapping("getName/{value}")
    public String getName(@PathVariable String value){
//        return dictService.getName("",value);
        return dictService.getName("",value);
    }

    //查询医院等级名称
    @GetMapping("getName/{dictCode}/{value}")
    public String getName(@PathVariable String dictCode,@PathVariable String value){
        return dictService.getName(dictCode,value);
    }

    @GetMapping("/exportData")
    public void exportData(HttpServletResponse response) throws IOException {
        dictService.exportData(response);
    }

    @PostMapping("/importData")
    public R importData(MultipartFile file) { //!!注意参数名字,空指针异常
        dictService.importData(file);
        return R.ok().message("导入成功");
    }

    /**
     * 根据数据字典的id，查询他的下级列表
     * （1）数据字典列表页面，显示一级数据列表
     * （2）点击某个数据字典，查询他的下级
     *
     * @param id
     * @return
     */
    @GetMapping("findChildData/{id}")
    public R findChildData(@PathVariable Long id) {
        List<Dict> list = dictService.findChildData(id);
        return R.ok().data("list", list);
    }

    @GetMapping("findAll")
    public R findAll() {
        return R.ok().data("list", dictService.list());
    }
}
