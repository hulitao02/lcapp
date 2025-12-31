package com.cloud.exam.aop;

import com.cloud.core.ApiResult;
import com.cloud.core.ExamConstants;
import com.cloud.exam.annotation.EditActivityAnnotation;
import com.cloud.exam.model.exam.Exam;
import com.cloud.exam.service.ExamService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dyl on 2021-6-8.
 * 根据活动状态判断是否可以修改活动
 */
@Component
@Aspect
public class EditActivityAspect {

    @Autowired
    private ExamService examService;
    @Pointcut("@annotation(com.cloud.exam.annotation.EditActivityAnnotation)")
    public  void pointCut(){
    }

    @Around("pointCut()&&@annotation(editActivityAnnotation)")
    public ApiResult editActivity(ProceedingJoinPoint pjp, EditActivityAnnotation editActivityAnnotation) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        //参数名数组
        String[] parameters =  methodSignature.getParameterNames();
        //参数值
        Object[] args = pjp.getArgs();
        Long examId = 1L;
        if(editActivityAnnotation.parameterType().equals("str")){
            examId = (Long) args[0];
        }else if(editActivityAnnotation.parameterType().equals("map")){
            Map<String,Long> map =(HashMap) args[0];
            examId = map.get("examId");
        }
        Exam byId = examService.getById(examId);
        ApiResult apiResult = null;
        if(byId.getExamStatus() > ExamConstants.ACTIVITY_NOT_LAUNCH){
            throw new IllegalArgumentException("当前活动状态下无法再次修改活动。");
            //return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_LAUNCH_YES.getResultCode(), ResultMesCode.EXAM_LAUNCH_YES.getResultMsg(), null);
        }else {
            Object proceed = pjp.proceed();
            apiResult  = (ApiResult)proceed;
            return apiResult;
        }
    }
}
