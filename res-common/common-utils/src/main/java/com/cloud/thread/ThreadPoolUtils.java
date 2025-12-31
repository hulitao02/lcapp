package com.cloud.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 线程池工具类
 */
public class ThreadPoolUtils {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取线程池
     * @param poolSize 线程数
     * @param maxPoolSize 最大线程数
     * @param aliveTime 活动时间
     * @param queueCapacity 队列长度
     * @return
     */
    public ThreadPoolExecutor getThreadPool(int poolSize, int maxPoolSize, int aliveTime, int queueCapacity){
        ThreadPoolExecutor exec = new ThreadPoolExecutor(poolSize, maxPoolSize, aliveTime, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity), Executors.defaultThreadFactory(), new CustomRejectedExecutionHandler());
        return exec;
    }

    /**
     * 队列已满 30s后尝试入列
     */
    class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
            try {
                logger.info("===========拒绝线程入队列中...");
                Thread.sleep(30000);
                if (!executor.isShutdown()) {
                    //再尝试入队
                    executor.submit(runnable);
                    logger.info("===========进入到队列中");
                }
            } catch (InterruptedException e) {
                logger.error("====线程中断：" + e.getMessage());
            } catch (Exception e){
                logger.error("====线程丢弃：" + e.getMessage());
            }
        }
    }
}
