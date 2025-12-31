package com.cloud.exam.weboffice.entity;

import lombok.Data;

@Data
public class WebOfficeObjectResult{

    /**
     * 错误码，0表示成功返回
     */
    private String errorCode;
    /**
     * 错误信息
     */
    private String errorMessage;

    private Object result;

}
