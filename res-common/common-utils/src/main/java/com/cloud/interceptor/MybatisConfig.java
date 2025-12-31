//package com.cloud.interceptor;
//
//import org.mybatis.spring.SqlSessionFactoryBean;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
////@Configuration
//public class MybatisConfig {
//
//    @Autowired
//    private MybatisInterceptorConfig mybatisInterceptorConfig;
//
//    @Bean
//    public SqlSessionFactoryBean sqlSessionFactoryBean() {
//        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
//        bean.setPlugins(mybatisInterceptorConfig);
//        return bean;
//    }
//
//
//}
