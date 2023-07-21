package com.atguigu.yygh.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.atguigu.yygh.oss.service.OssService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * @Description:
 * @Author: LiHao
 * @Date: 2023/7/12 11:23
 */
@Service
public class OssServiceImpl implements OssService {


    @Value("${yygh.oss.bucketName}")
    private String bucketName;

    @Value("${yygh.oss.endpoint}")
    private String endPoint;

    //时间作为路径，uuid+源文件名 作为完整的文件名  2023/07/09/uuid_xxxx.jpg
    @Override
    public String upload(MultipartFile file) {

        OSS ossClient = null;
        try {

            String endpoint = "https://" + endPoint;

            EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

            //根据当前日期，生成年月日路径
            String dir = new DateTime().toString("yyyy/MM/dd");

            String filename = file.getOriginalFilename();
            String objectName = dir + "/"+ UUID.randomUUID().toString().replaceAll("-","") + filename;


//            String filePath= "C:\\Users\\70208\\Desktop\\1.jpg";
//            InputStream inputStream = new FileInputStream(filePath);

            InputStream inputStream = file.getInputStream();//选择的文件对应的输入流

            ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            PutObjectResult result = ossClient.putObject(putObjectRequest);

            // 协议+桶名+域名
            String path  = "https://" + bucketName + "."+endPoint+"/" + objectName;

            return path;

        } catch (Exception oe) {
        }finally {
            //关闭连接
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        return null;
    }
}
