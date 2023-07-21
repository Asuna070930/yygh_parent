package com.atguigu.yygh.cmn.service;

import com.atguigu.yygh.model.cmn.Dict;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/3 9:56
 */
public interface DictService extends IService<Dict>{
    List<Dict> findChildData(Long id);

    void importData(MultipartFile file);

    void exportData(HttpServletResponse response) throws IOException;

    public Dict myUpdateById(Dict dict);

    String getName(String value);

    String getName(String dictCode, String value);

    List<Dict> findByDictCode(String dictCode);
}
