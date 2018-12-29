package com.pinyougou.shop.controller;

import com.pinyougou.utils.FastDFSClient;
import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传文件的controller
 */
@RestController //r +c
public class UploadController {

    @Value("${IMAGE_SERVER_URL}") //在给value 是给简单类型 注入哦
    private String IMAGE_SERVER_URL; //文件服务器地址


    @RequestMapping("/upload")
    public Result upload(MultipartFile file){
        //1获得文件的扩展名
        String originalFilename = file.getOriginalFilename();
        //2切割这个扩展名
        String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        //3、创建一个FastDFS的客户端
        try {
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            //4执行上传处理
            String path = fastDFSClient.uploadFile(file.getBytes(), extName);
            //5、拼接返回的url和ip地址，拼装成完整的url
            String url = IMAGE_SERVER_URL + path;
            return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }

    }
}
