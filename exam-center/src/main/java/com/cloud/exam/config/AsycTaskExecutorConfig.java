package com.cloud.exam.config;

import com.cloud.thread.TaskThreadPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

/**
 * 线程池配置、启用异步
 *
 * @author 数据管理
 *
 */
@EnableAsync(proxyTargetClass = true)
@Configuration
public class  AsycTaskExecutorConfig {
	Logger logger = LoggerFactory.getLogger(AsycTaskExecutorConfig.class);
	@Autowired
	private TaskThreadPoolConfig taskThreadPoolConfig;

	/**
	 * 线程池
	 *
	 * @author zhanggj
	 * @date 2018/4/3 15:17
	 */
	@Bean(name = "executor")
	public Executor asyncExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(taskThreadPoolConfig.getCorePoolSize());// 最小线程数
		taskExecutor.setMaxPoolSize(taskThreadPoolConfig.getMaxPoolSize());// 最大线程数
		taskExecutor.setKeepAliveSeconds(taskThreadPoolConfig.getKeepAliveSeconds());//允许的空闲时间
		taskExecutor.setQueueCapacity(taskThreadPoolConfig.getQueueCapacity());// 等待队列
		taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());//
		taskExecutor.setThreadNamePrefix("Publish-Executor-");
		taskExecutor.initialize();
		return taskExecutor;
	}

	@Bean(name = "threadPoolExecutor")
	public ThreadPoolExecutor threadPoolExecutor() {

		ThreadPoolExecutor exec = new ThreadPoolExecutor(taskThreadPoolConfig.getCorePoolSize(), taskThreadPoolConfig.getMaxPoolSize(), taskThreadPoolConfig.getKeepAliveSeconds(), TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<>(taskThreadPoolConfig.getQueueCapacity()), Executors.defaultThreadFactory(), new CustomRejectedExecutionHandler());

		return exec;
	}

	/**
	 * 队列已满 25s后尝试入列
	 */
	class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
		@Override
		public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
			try {
				logger.info("===========拒绝线程入队列中...");
				Thread.sleep(25000);
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
