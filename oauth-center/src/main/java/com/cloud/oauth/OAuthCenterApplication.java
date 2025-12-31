package com.cloud.oauth;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证中心
 * 
 * @author 数据管理 dmdata@example.com
 *
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@Slf4j
public class OAuthCenterApplication {

	public static void main(String[] args) {
		SpringApplication.run(OAuthCenterApplication.class, args);
		log.info("Oauth模块启动完成===================================");


	}

}