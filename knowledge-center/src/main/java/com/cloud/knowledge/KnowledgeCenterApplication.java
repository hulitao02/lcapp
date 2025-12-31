package com.cloud.knowledge;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.cloud")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.cloud")
@MapperScan("com.cloud.knowledge.dao")
@EnableScheduling
@Slf4j
public class KnowledgeCenterApplication {

	public static void main(String[] args) {

		SpringApplication.run(KnowledgeCenterApplication.class, args);
		log.info("图谱(knowledge-center)模块启动完成===================================");
	}
}