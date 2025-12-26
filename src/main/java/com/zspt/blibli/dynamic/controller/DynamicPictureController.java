package com.zspt.blibli.dynamic.controller;

import com.zspt.blibli.common.utils.FileUtils;
import com.zspt.blibli.main.config.FilePathConfig;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

@Slf4j
@RestController
@RequestMapping("/dynamic")
public class DynamicPictureController {

    @Resource
    private FilePathConfig filePathConfig;


    @GetMapping("/picture/{fileName}")
    public void getPicture(@PathVariable String fileName, HttpServletResponse response) {

        log.info( "image {}",fileName);
        try {
            // 1. 拼接本地图片完整路径
            File pictureFile = new File(filePathConfig.getPicturePath() + "/" + fileName);
            log.info( "image {}",pictureFile.getAbsolutePath());


            if (!pictureFile.exists()) {
                response.setStatus(404);
                return;
            }

            // 2. 设置响应头（告诉浏览器这是图片）
            response.setContentType("image/png"); // 根据图片类型调整（jpg/png等）
            response.setHeader("Content-Disposition", "inline; filename=" + fileName);

            // 3. 读取本地文件并写入响应流
            try (FileInputStream fis = new FileInputStream(pictureFile);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
            }
        } catch (Exception e) {
            response.setStatus(500);
        }
    }




 /*   @Resource
    private FilePathConfig filePathConfig;


    @GetMapping("/picture/{fileName}")
    public String  getPicture(@PathVariable String fileName) {

        log.info( "image {}",fileName);
        // 1. 拼接本地图片完整路径
        File pictureFile = new File(filePathConfig.getPicturePath() + "/" + fileName);
        log.info( "image {}",pictureFile.getAbsolutePath());


        if (!pictureFile .exists() || !pictureFile.isFile()) {
            return null;
        }
        // 获取图片类型（png/jpg等）
        String suffix = FileUtils.getFileExtension(fileName);
        String contentType = "image/" + suffix;
        // 兼容jpeg（jpg和jpeg是同一类型）
        if ("jpg".equals(suffix)) {
            contentType = "image/jpeg";
        }

        try (FileInputStream fis = new FileInputStream(pictureFile)) {
            // 读取文件字节 → 转Base64编码
            byte[] imageBytes = new byte[(int) pictureFile.length()];
            fis.read(imageBytes);
            String base64Code = Base64.getEncoder().encodeToString(imageBytes);
            // 拼接成img标签可直接使用的格式
            return "data:" + contentType + ";base64," + base64Code;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
*/

    }
