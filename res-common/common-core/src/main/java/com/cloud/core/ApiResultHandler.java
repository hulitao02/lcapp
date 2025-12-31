package com.cloud.core;


import com.cloud.exception.BussinessException;
import com.cloud.exception.ResultMesCode;

public class ApiResultHandler {

    public static ApiResult success(Object object) {
        ApiResult apiResult = new ApiResult();
        apiResult.setData(object);
        apiResult.setCode(200);
        apiResult.setMessage("请求成功");
        apiResult.setSuccess(true);
        return apiResult;
    }

    public static ApiResult error_data(Object object) {
        ApiResult apiResult = new ApiResult();
        apiResult.setData(object);
        apiResult.setCode(501);
        apiResult.setMessage("数据异常！！");
        apiResult.setSuccess(false);
        return apiResult;
    }

    public static ApiResult error(Object object) {
        ApiResult apiResult = new ApiResult();
        apiResult.setData(object);
        apiResult.setCode(500);
        apiResult.setMessage("请求失败");
        apiResult.setSuccess(false);
        return apiResult;
    }
    public static ApiResult success() {
        return success(null);
    }

    public static <T> ApiResult buildApiResult(Integer code, String message, T data) {
        ApiResult apiResult = new ApiResult();
        apiResult.setCode(code);
        apiResult.setMessage(message);
        apiResult.setData(data);
        apiResult.setSuccess(true);
        return apiResult;
    }


    //自定义异常返回的结果
    public static ApiResult defineError(BussinessException de){
        ApiResult result = new ApiResult();
        result.setSuccess(false);
        result.setCode(de.getErrorCode());
        result.setMessage(de.getErrorMsg());
        result.setData(null);
        return result;
    }

    //其他异常处理方法返回的结果
    public static ApiResult otherError(ResultMesCode errorEnum){
        ApiResult result = new ApiResult();
        result.setMessage(errorEnum.getResultMsg());
        result.setCode(errorEnum.getResultCode());
        result.setSuccess(false);
        result.setData(null);
        return result;
    }
}
