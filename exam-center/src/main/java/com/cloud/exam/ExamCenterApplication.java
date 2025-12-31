package com.cloud.exam;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients(basePackages = "com.cloud")
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.cloud")
@EnableScheduling
@MapperScan("com.cloud.exam.dao")
@Slf4j
public class ExamCenterApplication {

    public static void main(String[] args) {

        SpringApplicationBuilder builder = new SpringApplicationBuilder(ExamCenterApplication.class);
        builder.headless(false).run(args);
        log.info("(exam-center) 考试服务启动成功 +++++++++++++++++");

    }

}

