package com.atguigu.yygh.hosp.controller;

import com.atguigu.common.exphandler.YyghException;
import com.atguigu.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/6/25 18:42
 */

//医院设置接口
//原则: 后台管理系统使用的接口,通常已/admin开头
//@CrossOrigin
@Api(description = "医院设置接口文档")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
@Slf4j //Lombok下的一个日志注解,在当前类中可直接调用log对象及其方法
public class HospitalSetController {


    @Autowired
    private HospitalSetService hospitalSetService;


    @GetMapping("getApiUrl/{hoscode}")
    public String getApiUrl(@PathVariable String hoscode){
        HospitalSet hospitalSet = hospitalSetService.getByHoscode(hoscode);
        if(hospitalSet==null){
            throw new YyghException(20001,"该医院未开通医院设置");
        }
        return hospitalSet.getApiUrl();
    }


    //医院设置的批量删除, 示例数据[4,6,7]
    @ApiOperation(value = "根据id删除医院设置(逻辑删除)")
    @DeleteMapping("deleteByIds")
    public R deleteByIds(@ApiParam(name = "ids", value = "医院id集合") @RequestBody List<Long> ids) {
        boolean b = hospitalSetService.removeByIds(ids);
        return b ? R.ok().message("批量删除成功") : R.error().message("批量删除失败");
    }


    /**
     * 修改医院配置信息,根据id修改医院设置
     *
     * @param hospitalSet
     * @return
     */
    @PostMapping("updateHospset")
    public R updateHospset(@RequestBody HospitalSet hospitalSet) {
        //可以校验是否有id
        Long id = hospitalSet.getId();
        if (id == null) {
            return R.error().message("id为空");
        }
        boolean b = hospitalSetService.updateById(hospitalSet);
        return b ? R.ok().message("修改成功") : R.error().message("修改失败");
    }


    //医院设置列表的条件 + 分页查询(pageNum,pageSize) 分页从 1 开始
    //查询条件: 医院名称模糊查询 医院标号等值查询{hosname:xxx,hoscode:xx}
    //返回值: (data:{total:10,rows:{当前页的结果集}})
    @PostMapping("{pageNum}/{pageSize}")
    public R pageQuery(@PathVariable Integer pageNum, @PathVariable Integer pageSize, @RequestBody HospitalSetQueryVo hospitalSetQueryVo) {
        //1.创建page分页对象
        Page<HospitalSet> hospitalSetPage = new Page<>(pageNum, pageSize);

        //2.封装查询调教
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        String hosname = hospitalSetQueryVo.getHosname();
        String hoscode = hospitalSetQueryVo.getHoscode();

        //where hosname like ? and hoscode = ?
        //where hosname like null and hoscode = null 必须动态sql拼接(先判空 如果不为空 再拼接条件)
        if (!StringUtils.isEmpty(hoscode)) {//注意: 不为空
            queryWrapper.eq("hoscode", hoscode);//表中的列名
        }
        if (!StringUtils.isEmpty(hosname)) {
            queryWrapper.like("hosname", hosname);
        }

        //3.调用service中page方法(注意 配置分页插件)
        hospitalSetService.page(hospitalSetPage, queryWrapper);

        //4.从hospitalSetPage 对象中解析返回值
        List<HospitalSet> records = hospitalSetPage.getRecords();//当前页的结果集
        long total = hospitalSetPage.getTotal();//总记录数(前端的分页控件需要使用总记录数)

        return R.ok().data("total", total).data("rows", records);
    }


    //需求: 医院设置的锁定和解锁,修改status字段值, status取值范围 0 或 1 该接口中需要校验取值范围
    //参数: 医院设置的id + 希望修改成的状态值
    @GetMapping("lockHospset/{id}/{status}")
    public R updateStatus(@PathVariable Long id, @PathVariable Integer status) {

        //1.根据id查询到该医院设置,并且潘顿是否存在(未开通医院设置) 返回message =未开通医院设置 code20001
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        if (hospitalSet == null) {
            return R.error().message("未开通医院设置");
        }

        //2.Panduanstatus的取值范围,如果status不是 0 或 1 ,返回message=status 取值范围不正确 code=20001
        if (status != 1 && status != 0) {
            return R.error().message("status取值范围不正确");
        }
        //3.判断是否重复操作,如果status原本是 1 ,现在改成 1 ,这就是重复操作,返回message 重复操作 code=20001
        if (status == hospitalSet.getStatus()) {
            return R.error().message("重复操作");
        }
        //4.更新
        hospitalSet.setStatus(status);
        boolean b = hospitalSetService.updateById(hospitalSet);

        return b ? R.ok() : R.error();
    }


    //需求:开通医院设置(其实就是添加) 如果某个医院想入驻刀尚医通平台,必须有平台管理员开通权限
    //需要提供的数据:
    /*
        {
           "apiUrl": "http://127.0.0.1:9998", 医院端的接口地址,
               "contactsName": "张三",
               "contactsPhone": "13101102345",
               "hoscode": "10000", 医院编号,具备唯一性
               "hosname": "北京协和医院", 医院名称
               "signKey": "1" 医院的签名
        }
    */
    //需求: 请求方式 post 路径: saveHospset 参数: json
    @ApiOperation(value = "开通医院设置")
    @PostMapping("saveHospset")
    public R saveHospset(@ApiParam(name = "hospitalSet", value = "医院设置信息") @RequestBody HospitalSet hospitalSet) {
        hospitalSet.setStatus(1); //医院权限锁定  1: 正常
        boolean save = hospitalSetService.save(hospitalSet);
        return save ? R.ok().message("开通成功") : R.error().message("开通失败");
    }


    /**
     * 查询所有医院设置
     *
     * @return
     */
    @GetMapping("/findAll")
    public R findAll() {

        //不同级别日志,代表不同的严重程度
        log.info("info级别");
        log.error("error级别");
        log.warn("warn级别");

        List<HospitalSet> list = hospitalSetService.list();
        return R.ok().data("list", list);
    }


    /**
     * 根据 id 查询某个医院
     *
     * @param id
     * @return {code:xx,message:'',data:{item:{医院设置对象}}}
     */
    @GetMapping("{id}")
    public R findById(@PathVariable Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
//        Map<String, Object> map = new HashMap<>();
//        map.put("aa", 1);
//        map.put("bb", 2);
//        map.put("item", hospitalSet);

        return R.ok().message("根据id查询成功").data("item", hospitalSet);
    }

    /**
     * 根据 id 删除某个医院设置
     *
     * @param id
     * @return
     */
    @DeleteMapping("{id}")
    public R deleteById(@PathVariable Long id) {
        boolean b = hospitalSetService.removeById(id);
        return b ? R.ok().message("根据id删除成功") : R.error().message("根据id删除失败");
    }
}