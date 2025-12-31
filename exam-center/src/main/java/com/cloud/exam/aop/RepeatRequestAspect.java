package com.cloud.exam.aop;

import com.alibaba.fastjson.JSON;
import com.cloud.exam.annotation.RepeateRequestAnnotation;
import com.cloud.utils.Md5Util;
import com.cloud.utils.RedisUtils;
import com.cloud.utils.Validator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Created by dyl on 2021/08/26.
 */
@Component
@Aspect
public class RepeatRequestAspect {

    @Autowired
    private RedisUtils redisUtils;
    @Pointcut("@annotation(com.cloud.exam.annotation.RepeateRequestAnnotation)")
    public void servicePointCut(){}

    @Around("servicePointCut()&&@annotation(repeateRequestAnnotation)")
    public Object serviceRepeatLock(ProceedingJoinPoint pjp , RepeateRequestAnnotation repeateRequestAnnotation) throws Throwable {
        int i = repeateRequestAnnotation.extTimeOut();
        Method method = getMethod(pjp);
        String methodName = method.getName();
        Class aClass = getClass(pjp);
        String className = aClass.getName();
        //参数值
        Object[] args = pjp.getArgs();
        String paramsJson = JSON.toJSONString(args);
        String key = className+"_"+methodName+"_"+paramsJson;
        String hash = Md5Util.hash(key);
        Object requestCache = redisUtils.get(hash);
        //Object requestCache = Tools.getRequestCache(hash);
        if(!Validator.isEmpty(requestCache)){
            throw new IllegalArgumentException("请不要频繁提交。。。");
            //return ApiResultHandler.buildApiResult(ResultMesCode.REQUEST_SERVICE_REPEAT.getResultCode(), ResultMesCode.REQUEST_SERVICE_REPEAT.getResultMsg(), null);
        }else {
            Object proceed = pjp.proceed();
            redisUtils.set(hash,i,i);
            //Tools.putRequestCache(hash,i);
            return proceed;
        }
    }

    /**
     * 获取方法
     * @param joinPoint
     * @return
     */
    private Method getMethod(JoinPoint joinPoint){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method;
    }

    /**
     * 获取类名
     */
    private Class getClass(JoinPoint joinPoint){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<? extends MethodSignature> aClass = signature.getClass();
        return aClass;
    }
}
