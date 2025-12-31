package com.cloud.exam.weboffice.feign;

import com.cloud.config.FeignConfig;
import com.cloud.config.FormEncoder;
import com.cloud.exam.weboffice.feign.fallback.WebOfficeClientFallback;
import feign.codec.Encoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * WebOffice相关 客户端接口
 *
 * @Author 张争洋
 * @Date 2020-11-04
 */
@FeignClient(name = "name", url = "name", configuration = FeignConfig.class, fallback = WebOfficeClientFallback.class)
public interface WebOfficeClient {

    /**
     * 通用接口
     */
    @PostMapping(value = "/api.do", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String api(@RequestParam("fileId") String fileId, @RequestParam("jsonParams") String jsonParams);

    /**
     * 上传文档@RequestParam("filePath") String filePath, @RequestBody MultipartFile file
     */
    @PostMapping(value = "/uploadFile.do", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    String uploadFile( @RequestPart(value = "file") MultipartFile file);




    /**
     * 在线编辑/预览文档
     */
    @PostMapping(value = "/openFile.do", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String openFile(
            @RequestParam("fileId") String fileId,
//                                @RequestParam("fileName") String fileName,
            @RequestParam("filePath") String filePath,
            @RequestParam("readOnly") Boolean readOnly,
            @RequestParam("userId") String userId,
//                                @RequestParam("userAvatar") String userAvatar,
            @RequestParam("saveFlag") Boolean saveFlag);

     /**
     * 关闭文档
     */
    @PostMapping(value = "/closeFile.do", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String closeFile(@RequestParam("fileId") String fileId, @RequestParam("saveFlag") Boolean saveFlag);

    /**
     * 下载文档
     */
    @PostMapping(value = "/downloadFile.do", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<byte[]> downloadFile(@RequestParam("filePath") String filePath, @RequestParam(value = "isDeleteFile", defaultValue = "false") boolean isDeleteFile);

    /**
     * 打开接口
     */
    @PostMapping(value = "/api.do", consumes = MediaType.APPLICATION_JSON_VALUE)
    String apiOpenFile(@RequestParam("jsonParams") String jsonParams);

    /**
     * 将已经打开的doc文件生成pdf文档
     * return pdf路径 /weboffice/weboffice3.2_build189/weboffice_1/temp/wo3.2_temp/$$B4G56I937XT6689/B4G56I937XT6689.pdf
     */
    @PostMapping(value = "/test.do", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String test(@RequestParam("fileId") String fileId);

    /**
     * @Description TODO(描述) 上传文件需要 指定 Encoder
     */
    class MultipartSupportConfig {

        @Autowired
        private ObjectFactory<HttpMessageConverters> messageConverters;

        @Bean
        public Encoder springFormEncoder() {
            return new FormEncoder();
        }
    }
}
