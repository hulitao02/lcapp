package com.cloud.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 管理后台
 *
 * @author 数据管理 dmdata@example.com
 *
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.cloud")
@EnableFeignClients(basePackages = "com.cloud")
@MapperScan("com.cloud.backend.dao")
public class ManageBackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(ManageBackendApplication.class, args);
		System.out.println("维护管理子系统启动完毕！");
	}

}
