package com.cloud.exam.weboffice.feign.fallback;

import com.cloud.exam.weboffice.entity.WebOfficeObjectResult;
import com.cloud.exam.weboffice.feign.WebOfficeClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.text.MessageFormat;

/**
 * weboffice服务客户端接口熔断器
 *
 * @Author 张争洋
 * @Date 2019-06-01 19:37:55
 */
@Component
@Slf4j
public class WebOfficeClientFallback implements WebOfficeClient, FallbackFactory<WebOfficeClient> {

    /**
     * 异常栈
     */
    private Throwable throwable;
    private final String CONTROLLER_COMMONT = "weboffice服务客户端(WebOfficeClient)";

    public WebOfficeClientFallback() {
    }

    public WebOfficeClientFallback(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public WebOfficeClientFallback create(Throwable throwable) {
        return new WebOfficeClientFallback(throwable);
    }

    /**
     * 上传文件
     */
    @Override
    public String  uploadFile(MultipartFile multipartFile) {
    /*    String methodCommont = "上传文件(uploadFile)";
        log.error("\r\n调用 {}->{} 接口失败! 参数：filePath:{}\r\n", CONTROLLER_COMMONT, methodCommont, filePath, throwable);
        WebOfficeObjectResult result = new WebOfficeObjectResult();
        result.setErrorCode("1");
        result.setErrorMessage(MessageFormat.format("调用 {0}->{1} 接口失败，错误信息为:{2}",CONTROLLER_COMMONT, methodCommont, throwable.getMessage()));
        return result.toString();*/
        return null;
    }

    /**
     * weboffice在线编辑/预览
     */
    @Override
    public String openFile(String fileId,
                           //String fileName,
                           String filePath, Boolean readOnly, String userName,
//                         String userAvatar,
                           Boolean saveFlag) {
        String methodCommont = "weboffice在线编辑/预览(open)";
        log.error("\r\n调用 {}->{} 接口失败! \r\n", CONTROLLER_COMMONT, methodCommont, throwable);
        WebOfficeObjectResult result = new WebOfficeObjectResult();
        result.setErrorCode("1");
        result.setErrorMessage(MessageFormat.format("调用 {0}->{1} 接口失败，错误信息为:{2}",CONTROLLER_COMMONT, methodCommont, throwable.getMessage()));
        return result.toString();
    }

    /**
     * 关闭
     */
    @Override
    public String closeFile(String fileId, Boolean saveFlag){
        String methodCommont = "关闭(closeFile)";
        log.error("\r\n调用 {}->{} 接口失败! 参数：fileId:{}, saveFlag:{}\r\n", CONTROLLER_COMMONT, methodCommont, fileId, saveFlag, throwable);
        WebOfficeObjectResult result = new WebOfficeObjectResult();
        result.setErrorCode("1");
        result.setErrorMessage(MessageFormat.format("调用 {0}->{1} 接口失败，错误信息为:{2}",CONTROLLER_COMMONT, methodCommont, throwable.getMessage()));
        return result.toString();
    }

    /**
     * 查看是否已打开
     */
    @Override
    public String api(String fileId, String param){
        String methodCommont = "查看是否已打开(isOpen)";
        log.error("\r\n调用 {}->{} 接口失败! 参数：fileId:{}, jsonParams:{}\r\n", CONTROLLER_COMMONT, methodCommont, fileId, param, throwable);
        WebOfficeObjectResult result = new WebOfficeObjectResult();
        result.setErrorCode("1");
        result.setErrorMessage(MessageFormat.format("调用 {0}->{1} 接口失败，错误信息为:{2}",CONTROLLER_COMMONT, methodCommont, throwable.getMessage()));
        return result.toString();
    }

    /**
     * 下载文件
     */
    @Override
    public ResponseEntity<byte[]> downloadFile(String filePath, boolean isDeleteFile) {
        String methodCommont = "下载文件(downloadFile)";
        log.error("\r\n调用 {}->{} 接口失败! 参数：fileId:{}, isDeleteFile:{}\r\n", CONTROLLER_COMMONT, methodCommont, filePath, isDeleteFile, throwable);
        return null;
    }

    @Override
    public String apiOpenFile(String jsonParams) {
        return null;
    }

    @Override
    public String test(String fileId) {
        return null;
    }

}
