package com.cloud.aop;

import com.cloud.utils.CollectionsCustomer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by LYD on 2022/10/26
 * AOP 拦截转化大小写
 */
@Component
@Aspect
@Slf4j
public class SensitivAspect {


    // 拦截Dao层 ，返回的MAP 和 List<Map>  类型
    @Around("execution(java.util.List com.cloud.*.dao..*.*(..)) || execution(java.util.Map com.cloud.*.dao..*.*(..))")
    public Object finishQuestionByhost(ProceedingJoinPoint pjp) throws Throwable {
        // 先执行 ，获得结果
        Object proceed = pjp.proceed();

        //  通过返回值的结果，强制转换，转换MAP或者List<Map>
        MethodSignature methodSignature = (MethodSignature)pjp.getSignature();

        Method method = methodSignature.getMethod();
        String packetName = method.getDeclaringClass().getName();
        String methodName = packetName+"."+methodSignature.getName();
        Class<?> returnType = method.getReturnType();
        /**
         *  拦截MAP 和 List 后强制转换
         */
        String returnTypeName = returnType.getName();
        if(returnTypeName.equals("java.util.Map")){
            //  强制转换Map
            try{
                Map proceed1 = (Map) proceed;
                Map map = CollectionsCustomer.builder().build().mapToLowerCase(proceed1);
                log.debug("[MAP 转换] 拦截的方法名称:[{}], 返回值类型:[{}]",methodName,returnType);
                proceed = map;
            }catch (Exception e){
            }
        }else{
//
            try{
                List<Map> proceed_1 = (List<Map>) proceed;
                if(!CollectionUtils.isEmpty(proceed_1)){
                    List<Map> listMap = CollectionsCustomer.builder().build().listMapToLowerCase(proceed_1);
                    log.debug("[List<Map> 转换] 拦截的方法名称:[{}], 返回值类型:[{}]",methodName,returnType);
                    proceed = listMap;
                }
            }catch (Exception e1){
            }
        };
        return proceed;
    }


}
