package com.cloud.file.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
@Data
public class FdfsConfig {

    @Value("${fileServerUrl}")
    private String fileServerUrl;
}
