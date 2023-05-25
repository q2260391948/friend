package com.my.xiaozhang.controller;

import com.my.xiaozhang.utils.QiniuOssUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


@Controller
@RequestMapping("upload")
@CrossOrigin(origins = {"http://localhost:3000/","http://124.221.169.181/"})
public class uploadimg {
    @PostMapping("/img")
    @ResponseBody
    public void up1img(@RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        System.out.println("收到了请求上传单张图片==");
        System.out.println(file);
        if (file.isEmpty()) {
            return;
        }
        String fileName = file.getOriginalFilename();
        InputStream inputStream = file.getInputStream();
        QiniuOssUtils utils = new QiniuOssUtils();

        String upload = utils.upload(inputStream, fileName);
        System.out.println(upload);
    }
}