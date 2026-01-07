package com.jinjin.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration object, automatically inject values from configuration file
 */
@Data
@Component
@ConfigurationProperties(prefix = "jinjin.aws.s3")
public class AwsS3Properties {
    private String bucketName;
    private String region;
}
