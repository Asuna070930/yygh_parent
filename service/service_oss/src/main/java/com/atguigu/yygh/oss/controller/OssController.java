package com.atguigu.yygh.oss.controller;

import com.atguigu.common.result.R;
import com.atguigu.yygh.oss.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/12 11:22
 */
@RestController
@RequestMapping("/api/oss")
public class OssController {


    @Autowired
    OssService ossService;

    /**
     * 选择本地的本地，上传到upload接口，该接口继续将文件上传到阿里云oss，返回url地址
     *
     * @return
     */
    @PostMapping("upload")
    public R upload(MultipartFile file) {
        String url = ossService.upload(file);
        return R.ok().data("url", url);
    }
}
