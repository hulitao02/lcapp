package com.cloud.model.config;


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
				.antMatchers(PermitAllUrl.permitAllUrl("/backend-anon/**", "/favicon.ico", "/assets/**", "/css/**", "/js/**",
						"/fonts/**", "/layui/**", "/img/**", "/pages/**", "/pages/**/*.html", "/*.html", "/knowledgepoints/**", "/findDict",
						"/delknowledge/**",
						"/knowledge/**",
						"/other/**",
						"/modelKp/**",
						"/data/**",
						"/delproperty/**",
						"/knowledge/getmjImage/**",
						"/findIntDirectById/**")).permitAll()
				.anyRequest().authenticated().and().httpBasic();

		http.headers().frameOptions().sameOrigin();
	}

}
