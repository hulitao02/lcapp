package com.cloud.model.config;

//@Configuration
public class EsConfig {

//    @Value(value = "${es.domain}")
    private String ES_DOMAIN;


//    @Bean
    public EsConfig IniteConfig() {


        return new EsConfig();
    }


}
