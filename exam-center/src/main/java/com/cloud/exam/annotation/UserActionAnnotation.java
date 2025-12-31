package com.cloud.exam.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by dyl on 2021-5-26.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UserActionAnnotation {
    /**
     * 用户行为标识
     * @return
     */
    int  ActionType() default 0;

    /**
     * 活动类型
     */
    int  examType() default 0;
}
