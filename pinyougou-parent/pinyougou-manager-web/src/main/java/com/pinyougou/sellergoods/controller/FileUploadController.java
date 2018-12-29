package com.pinyougou.sellergoods.controller;


import com.pinyougou.utils.FastDFSClient;
import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 */
@RestController
public class FileUploadController {

    @Value("${IMAGE.IMAGE_SERVER_URL}")
    private String IMAGE_SERVER_URL;

    @RequestMapping("/upload")
    public Result upload(MultipartFile file){
        //核心的代码：1.获取文件的字节数组 2.通过fastdfs的上传API 调用
        try {
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");

            //获取字节数组
            byte[] bytes = file.getBytes();
            //获取原文件的扩展名
            String originalFilename = file.getOriginalFilename();//1234.jpg
            String extName =  originalFilename.substring(originalFilename.lastIndexOf(".")+1);//jpg
            String path = fastDFSClient.uploadFile(bytes, extName);//     group1/M00/00/05/wKgZhVwgfNuAIh-TAAClQrJOYvs563.jpg
            //拼接全路径
            String realPath = "http://192.168.25.133/"+path;

            return new Result(true,realPath);

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }
    }
}
