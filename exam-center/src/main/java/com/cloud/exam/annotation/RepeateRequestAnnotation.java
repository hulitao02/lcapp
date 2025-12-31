package com.cloud.exam.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by dyl on 2021-6-9.
 * 接口幂等 防止重复提交
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeateRequestAnnotation {

    /**
     * 锁的超时时间
     * @return
     */
    int  extTimeOut() default 10;

    /**
     * 忽略的参数
     * @return
     * int[] ignoreIndex();
     */

}
