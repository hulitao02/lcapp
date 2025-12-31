package com.cloud.exam.config;


import com.cloud.core.PermitAllUrl;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

import javax.servlet.http.HttpServletResponse;

/**
 * 资源服务配置
 *
 * @author 数据管理
 *
 */
@EnableResourceServer
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().exceptionHandling()
				.authenticationEntryPoint(
						(request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
				.and().authorizeRequests()
				//.antMatchers(PermitAllUrl.permitAllUrl("/paperSave/**","/screenRecording")).permitAll() // 放开权限的url
				.antMatchers(PermitAllUrl.permitAllUrl("/**/**","/getImageAnnotionQuestion",
						// 知识点删除
						"/delKnowledgePoint/**",
						"/question/**",
						"/question",
						"/questionIds"
				)).permitAll() // 放开权限的url
				.anyRequest().authenticated().and().httpBasic();
	}

}
