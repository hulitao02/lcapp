package com.cloud.gateway.aop;

import com.cloud.gateway.feign.UserClient;
import com.cloud.model.user.AppUser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * Created by dyl on 2021/08/30.
 */
@Component
@Aspect
public class PasswordVerifyAspect {

    @Autowired
    private UserClient userClient;

    @Around("execution(* com.cloud.gateway.controller.TokenController.login(..))")
    public Object  verifyPassword(ProceedingJoinPoint  pjp) throws Throwable{
        //参数值
        Object[] args = pjp.getArgs();
        String  loginname = (String) args[0];
        String  password = (String) args[1];
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(password);
        List<AppUser> appUsersByloginname = userClient.getAppUsersByloginname(loginname);
        HashMap<String,Object>  map = new HashMap<>();
        map.put("code", 400);
        map.put("message", "用户名或密码错误，请重新输入。");
        if(appUsersByloginname==null || appUsersByloginname.size()<1){
            return map;
        }else {
            boolean b = appUsersByloginname.stream().anyMatch(e -> passwordEncoder.matches(password,e.getPassword()));
            if(b){
                Object proceed = pjp.proceed();
                return proceed;
            }else {
                return map;
            }
        }

    }
}
