package com.cloud.log;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

/**
 * 日志中心
 * 
 * @author 数据管理 dmdata@example.com
 *
 */
@EnableDiscoveryClient
@SpringBootApplication
@EnableRabbit
@ComponentScans(value = {@ComponentScan(value = "com.cloud.log.*")})
public class LogCenterApplication {

	public static void main(String[] args) {
		SpringApplication.run(com.cloud.log.LogCenterApplication.class, args);
	}

}