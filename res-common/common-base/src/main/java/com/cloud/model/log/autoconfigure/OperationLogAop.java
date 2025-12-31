package com.cloud.model.log.autoconfigure;


import com.cloud.model.log.OperationLog;
import com.cloud.model.log.OperationLogAnnotation;
import com.cloud.model.user.constants.UserCenterMq;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Aspect
@Service
public class OperationLogAop {
    private static final Logger logger = LoggerFactory.getLogger(OperationLogAop.class);
    @Autowired
    private AmqpTemplate amqpTemplate;
    /**
     * 环绕带注解 @OperationLogAnnotation的方法做aop
     */
    @Around(value = "@annotation(com.cloud.model.log.OperationLogAnnotation)")
    public Object logSave(ProceedingJoinPoint joinPoint) throws Throwable {
        OperationLog log = new OperationLog();
        log.setCreateTime(new Date());
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        OperationLogAnnotation logAnnotation = methodSignature.getMethod().getDeclaredAnnotation(OperationLogAnnotation.class);
        if (logAnnotation.recordParam()) { // 是否要记录方法的参数数据
            String[] paramNames = methodSignature.getParameterNames();// 参数名
            if (paramNames != null && paramNames.length > 0) {
                Object[] args = joinPoint.getArgs();// 参数值

                Map<String, Object> params = new HashMap<>();
                for (int i = 0; i < paramNames.length; i++) {
                    Object value = args[i];
                    if (value instanceof Serializable) {
                        if(paramNames[i].equals("name"))
                        {
                            log.setName(value.toString());//设置名称(唯一性)
                            continue;
                        }
                        params.put(paramNames[i], value);
                    }
                }
                try {
                    log.setContent(params.get("content").toString()); // 以json的形式记录参数
                } catch (Exception e) {
                    logger.error("记录参数失败：{}", e.getMessage());
                }
            }
        }

        try {
            Object object = joinPoint.proceed();// 执行原方法
            log.setFlag(Boolean.TRUE);
            return object;
        } catch (Exception e) { // 方法执行失败
            log.setFlag(Boolean.FALSE);
            throw e;
        } finally {
            // 异步将Log对象发送到队列
            CompletableFuture.runAsync(() -> {
                try {
                    amqpTemplate.convertAndSend(UserCenterMq.MQ_EXCHANGE_USER, UserCenterMq.ROUTING_KEY_USER_OPERATION_LOG, log);
                    logger.info("发送操作日志到队列：{}", log);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            });

        }

    }
}
