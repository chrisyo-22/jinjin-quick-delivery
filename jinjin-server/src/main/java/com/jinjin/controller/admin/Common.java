package com.jinjin.controller.admin;


import com.jinjin.result.Result;
import com.jinjin.utils.AwsS3Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RequestMapping("/admin/common")
@RestController
public class Common {

    @Autowired
    private AwsS3Utils awsS3Utils;

    @PostMapping("/upload")
    public Result upload(MultipartFile file) {

        String originalName = file.getOriginalFilename();
        log.info("originalName: {}", originalName);
        String extName;
        if (originalName == null || originalName.isEmpty()) {
            extName = ".png";
        } else {
            extName = originalName.substring(originalName.lastIndexOf("."));
        }
        try {
            // use aws s3 to upload a file
            String objectName = UUID.randomUUID().toString() + extName;
            String url = awsS3Utils.upload(file.getBytes(), objectName);
            return Result.success(url);
        } catch (IOException e) {
            log.info("upload file error: {}", e.getMessage());
            return Result.error("file upload failed");
        }

    }
}
