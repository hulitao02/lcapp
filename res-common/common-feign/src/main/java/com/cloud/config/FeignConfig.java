package com.cloud.config;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.DispatcherServlet;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

/**
 * 解决feign调用token丢失问题
 *
 * @Auther: 张争洋
 * @Date: 2018/9/14 16:48
 */
@Configuration
public class FeignConfig implements RequestInterceptor {

//    @Autowired
//    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Autowired
    RequestContextFilter requestContextFilter;

    @Autowired
    DispatcherServlet dispatcherServlet;

    @PostConstruct
    public void init() {
        // 设置线程继承属性为true，便于子线程获取到父线程的request,两个都设置为了保险。
        requestContextFilter.setThreadContextInheritable(true);
        dispatcherServlet.setThreadContextInheritable(true);
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(requestAttributes,true);
        HttpServletRequest request = requestAttributes.getRequest();
        requestTemplate.header(HttpHeaders.AUTHORIZATION,request.getHeader(HttpHeaders.AUTHORIZATION));
    }

//    @Bean
//    public SpringFormEncoder feignFormEncoder(){
//        return new SpringFormEncoder(new SpringEncoder(messageConverters));
//    }



}
