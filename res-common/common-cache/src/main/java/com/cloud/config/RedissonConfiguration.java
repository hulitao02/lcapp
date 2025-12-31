package com.cloud.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by dyl on 2021/06/15.
 * 单机模式下获取redissonClient
 */
@Configuration
@RefreshScope
public class RedissonConfiguration {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.password}")
    private String password;
    @Value("${spring.redis.port}")
    private String port;
    @Value("${spring.redis.timeout}")
    private String timeout;

    @Bean
    public RedissonClient redisson(){
        Config config = new Config();
        config.useSingleServer().setAddress(String.format("redis://%s:%s",host,port))
                .setPassword(password)
                .setTimeout(Integer.valueOf(timeout)).setRetryAttempts(3).setPingConnectionInterval(1000);
        return Redisson.create(config);
    }
}
