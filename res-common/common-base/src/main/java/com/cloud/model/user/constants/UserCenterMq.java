package com.cloud.model.user.constants;

/**
 * rabbitmq的exchange和routing key定义
 * 
 * @author 数据管理
 *
 */
public interface UserCenterMq {

	/**
	 * 用户系统exchange名
	 */
	String MQ_EXCHANGE_USER = "user.topic.exchange";

	/**
	 * 角色删除routing key
	 */
	String ROUTING_KEY_ROLE_DELETE = "role.delete";

	/**
	 * 日志key
	 */
	String ROUTING_KEY_USER_LOGINFO = "user.loginfo";

	String ROUTING_KEY_USER_OPERATION_LOG = "operation.log";
}
