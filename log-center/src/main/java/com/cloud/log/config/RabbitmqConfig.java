package com.cloud.log.config;

import com.cloud.model.log.constants.LogQueue;
import com.cloud.model.user.constants.UserCenterMq;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbitmq配置
 * 
 * @author 数据管理
 *
 */
@Configuration
public class RabbitmqConfig {

	/**
	 * 声明队列，此队列用来接收角色删除的消息
	 *
	 * @return
	 */
	@Bean
	public Queue logQueue() {
		Queue queue = new Queue(LogQueue.LOG_QUEUE);

		return queue;
	}


	@Bean
	public Queue operationLogQueue() {
		Queue queue = new Queue(LogQueue.OPERATION_LOG_QUEUE);
		return queue;
	}

	@Bean
	public TopicExchange logTopicExchange() {
		return new TopicExchange(UserCenterMq.MQ_EXCHANGE_USER);
	}

	/**
	 * 将角色删除队列和用户的exchange做个绑定
	 *
	 * @return
	 */
	@Bean
	public Binding bindingRoleDelete() {
		Binding binding = BindingBuilder.bind(logQueue()).to(logTopicExchange())
				.with(UserCenterMq.ROUTING_KEY_USER_LOGINFO);
		return binding;
	}

	/**
	 * 将操作日志队列和用户的exchange做个绑定
	 *
	 * @return
	 */
	@Bean
	public Binding bindingOperationLog() {
		Binding binding = BindingBuilder.bind(operationLogQueue()).to(logTopicExchange())
				.with(UserCenterMq.ROUTING_KEY_USER_OPERATION_LOG);
		return binding;
	}
}
