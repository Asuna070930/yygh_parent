package com.atguigu.yygh.oss.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/12 11:23
 */
public interface OssService {
    String upload(MultipartFile file);
}
