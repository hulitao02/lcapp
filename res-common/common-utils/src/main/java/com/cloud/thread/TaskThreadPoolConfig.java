package com.cloud.thread;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * Created by dyl on 2021/04/20.
 */
//用于从config服务刷新配置
@RefreshScope
@Configuration
public class TaskThreadPoolConfig {

    /**
     * 核心线程数
     */
    @Value("${thread.core_pool_size}")
    private int corePoolSize;
    /**
     * 最大线程数
     */
    @Value("${thread.max_pool_size}")
    private int maxPoolSize;
    /**
     * 允许的空闲时间
     */
    @Value("${thread.keep_alive_seconds}")
    private int keepAliveSeconds;
    /**
     * 队列最大长度
     */
    @Value("${thread.queue_capacity}")
    private int queueCapacity;

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

    public void setKeepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }
}
