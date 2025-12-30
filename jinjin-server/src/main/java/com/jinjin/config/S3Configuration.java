package com.jinjin.config;

import com.jinjin.properties.AwsS3Properties;
import com.jinjin.utils.AwsS3Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class S3Configuration {



    @Bean
    public AwsS3Utils awsS3Utils(AwsS3Properties awsS3Properties) {
        log.info("init aws s3 utils");
        AwsS3Utils awsS3Utils = new AwsS3Utils(awsS3Properties.getBucketName(), awsS3Properties.getRegion());
        return awsS3Utils;
    }
}
