package com.atguigu.gmall0715.manage.controller;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class FileUploadController {

    @Value("${fileServer.url}")
    private String fileUrl;

    @RequestMapping("/fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException, MyException {
        String imgUrl = fileUrl;
        if (file == null) {
            return null;
        }

        //读取图片服务器的地址
        String configFile = this.getClass().getResource("/tracker.conf").getFile();
        ClientGlobal.init(configFile);
        //客户端
        TrackerClient trackerClient = new TrackerClient();
        //服务器
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageClient storageClient = new StorageClient(trackerServer, null);
        //上传的图片
        //String orginalFilename = "E:\\12498\\20160214_IMG_0633.JPG";
        String originalFilename = file.getOriginalFilename();
        String extName = StringUtils.substringAfterLast(originalFilename, ".");
        String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
        for (int i = 0; i < upload_file.length; i++) {
            String path = upload_file[i];
            imgUrl += "/" + path;
        }
        return imgUrl;
    }
}
