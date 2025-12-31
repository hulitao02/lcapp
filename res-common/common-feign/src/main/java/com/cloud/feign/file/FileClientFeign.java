package com.cloud.feign.file;


import com.cloud.config.FeignSupportConfig;
import com.cloud.config.FormEncoder;
import com.cloud.core.ApiResult;
import com.cloud.model.file.TifFastdfsRelation;
import feign.codec.Encoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Component
@FeignClient(name = "file-center",configuration = FeignSupportConfig.class)
public interface FileClientFeign {

    @GetMapping("/files/saveFile")
    void saveFile(@RequestParam Map<String, Object> fileParam);

    @PostMapping(value = "/files/upload",produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String upload(@RequestPart("file") MultipartFile file);
    /**
     *  查询
     * @param tifId
     * @return
     */
    @GetMapping(value = "/files/getTifFastdfsRelationByTifId")
    public TifFastdfsRelation getTifFastdfsRelationByTifId(@RequestParam(value="tifId") String tifId);


    @PostMapping(path = "/files/tifProcessMultiFile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResult tifProcessMultiFile(@RequestPart("multipartFile") MultipartFile multipartFile);

    @PostMapping(value = "/files/copyPic")
    String copyPic(@RequestBody String sourcePath);


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
