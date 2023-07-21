package com.atguigu.yygh.cmn.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/7 22:48
 */
@FeignClient(value = "service-cmn") // 数据字典服务的spring.application.name
public interface DictFeignClient {

    @GetMapping("/admin/cmn/getName/{value}") //注意：路径=类+方法
    public String getName(@PathVariable String value);

    @GetMapping("/admin/cmn/getName/{dictCode}/{value}")
    public String getName(@PathVariable String dictCode,@PathVariable String value);
}