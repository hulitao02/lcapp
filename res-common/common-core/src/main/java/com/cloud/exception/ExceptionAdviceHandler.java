package com.cloud.exception;


import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionAdviceHandler {


    public  static Logger logger  = LoggerFactory.getLogger(ExceptionAdviceHandler.class);

    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> badRequestException(IllegalArgumentException exception) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("code", HttpStatus.BAD_REQUEST.value());
        data.put("message", exception.getMessage());

        return data;
    }

    @ExceptionHandler({NullPointerException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> nullPointerException(NullPointerException exception) {
        exception.printStackTrace();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("code", ResultMesCode.NULLPOINTER_DATA_WRONG.getResultCode());
        data.put("message", "数据存在异常，请检查后重试!");

        return data;
    }

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(value = BussinessException.class)
    public ApiResult bizExceptionHandler(BussinessException e) {
        return ApiResultHandler.defineError(e);
    }

//    /**
//     * 处理其他异常
//     */
//    @ExceptionHandler(value = Exception.class)
//    public ApiResult exceptionHandler(Exception e) {
//        e.printStackTrace();
//        return ApiResultHandler.otherError(ResultMesCode.INTERNAL_SERVER_ERROR);
//    }

    /**
     * 参数绑定错误
     */
    @ExceptionHandler(value = BindException.class)
    public Map<String, Object> bindExceptionHandler(Exception exception) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("code", ResultMesCode.PARAM_IS_ERROR.getResultCode());
        data.put("message", exception.getMessage());
        return data;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> badRequestException(MethodArgumentNotValidException exception) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("code", ResultMesCode.PARAM_IS_ERROR.getResultCode());
        data.put("message", exception.getBindingResult().getFieldError().getDefaultMessage());

        return data;
    }
}
