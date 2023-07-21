package com.atguigu.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictReadListener;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/3 9:57
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

//    @Autowired
//    RedisTemplate redisTemplate;

    @Autowired
    DictReadListener dictReadListener;

    //需求: 根据id查询对应的数据字典列表是,先根据参数id作为key  去redis缓存数据库中查询对应的value(List<Dict>),如果存在直接return该value
    //如果不存在,从mysql中查询list集合,存储到redis中 key: 参数id  value: list集合
    //调用该方法时,方法体不一定会执行,现根据参数作为key,去redis中查询value是否存在,如果存在直接返回
    //如果不存在方法体才会呗执行, 最后来利用参数生成key,利用return 返回的数据作为value,存入redis中
    //key: 用来指定当前命名空间下的缓存key的生成的规则 例如: dict_id值  dict_1 表示id=1的数据字典集合
    @Cacheable(cacheNames = "findChildData_cache", key = "'dict_'+#id")
    @Override
    public List<Dict> findChildData(Long id) {

        //1.根据parent_id = ? 查询数据字典列表
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", id);
        List<Dict> lsit = baseMapper.selectList(queryWrapper);

        //2.判断每个节点是否存在下级 如果存在,hasChildren = true
        lsit.forEach(dict -> {
            boolean bo = this.isHasChildren(dict);
            dict.setHasChildren(bo);
        });
        return lsit;
    }


    //导入数据接口
    //导入数据之后,将 findChildData_cache 命名空间下的缓存全部清空
    //方法体每次都会执行,并且在执行方法体前,会将 findChildData_cache 明明空间下的所有缓存清空
    //beforeInvocation = true : 方法体执行之前
    //llEntries = true : 全部清空
    @CacheEvict(cacheNames = "findChildData_cache", beforeInvocation = true, allEntries = true)
    @Override
    public void importData(MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            EasyExcel.read(inputStream, DictEeVo.class, dictReadListener).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exportData(HttpServletResponse response) throws IOException {
        //1.准备excel文档中的数据,也就是dict中的所有数据字典
        List<Dict> dictList = baseMapper.selectList(null);

        //2.dictList转成DictEevo泛型的集合
        List<DictEeVo> dictEeVoList = new ArrayList<>();
        dictList.forEach(dict -> {
            DictEeVo dictEeVo = new DictEeVo();
            //复制文件
            BeanUtils.copyProperties(dict, dictEeVo);
            dictEeVoList.add(dictEeVo);
        });

        //3.响应输出流(文件下载是,需要使用HttpServletResponse对象中的响应输出流)
        ServletOutputStream outputStream = response.getOutputStream();

        //4.文件下载,需要设置一些响应头
        response.setContentType("application/vnd.ms-excel");
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("数据字典.xlsx", "utf-8"));

        //5.EasyExcel的写数据的API
        EasyExcel.write(outputStream, DictEeVo.class).sheet("数据字典列表").doWrite(dictEeVoList);
    }


    //逐个添加数据字段dict对象，每次添加都会向redis中添加一组k-
    //并且更新dict时，redis中的缓存也要更新

    //方法体每次都执行，并且每次都向redis中添加一组k-v，如果k存在，就相当于更新了缓存;
    @CachePut(cacheNames = "my_test", key = "'mydict_'+#dict.id")
    @Override
    public Dict myUpdateById(Dict dict) {
        baseMapper.updateById(dict);
        return dict;
    }

    @Override
    public String getName(String value) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("value", value);

        List<Dict> dictList = baseMapper.selectList(queryWrapper);

        //校验
        if (dictList.size() > 1) {
            return "该value=" + value + "不唯一";
        }

        return dictList.size() > 0 ? dictList.get(0).getName() : "该value没有对应的名称";
    }

    //SELECT * FROM `dict` WHERE VALUE = 1 AND parent_id = ( SELECT id FROM dict WHERE dict_code = 'Hostype' )
    @Override
    public String getName(String dictCode, String value) {

        if (StringUtils.isEmpty(dictCode)) {
            QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("value", value);
            return baseMapper.selectOne(queryWrapper).getName();
        } else {
            QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("VALUE", value);
            queryWrapper.eq("parent_id", this.getDictByDictCode(dictCode).getId());

            return baseMapper.selectOne(queryWrapper).getName();
        }
    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        //SELECT * FROM `dict` WHERE parent_id = (SELECT id FROM dict WHERE dict_code = 'Province')

        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        Dict dictByDictCode = this.getDictByDictCode(dictCode);
        queryWrapper.eq("parent_id", dictByDictCode.getId());

        return baseMapper.selectList(queryWrapper);
    }

    //根据dictCode查询到某一个数据字典对象
    private Dict getDictByDictCode(String dictCode) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dict_code", dictCode);
        return baseMapper.selectOne(queryWrapper);
    }

    //查新当前dict是否有下级,返回bo
    private boolean isHasChildren(Dict dict) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", dict.getId());

        return baseMapper.selectCount(queryWrapper) > 0;

    }
}
