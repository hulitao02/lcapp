package com.cloud.config;


import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class FeignSupportConfig {


    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;


    @Scope("prototype")
    @Bean
    public SpringFormEncoder feignFormEncoder(){

        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }



}
