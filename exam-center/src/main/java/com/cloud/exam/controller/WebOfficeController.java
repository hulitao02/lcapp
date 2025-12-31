package com.cloud.exam.controller;

import com.cloud.exam.service.WebOfficeService;
import com.cloud.exam.weboffice.entity.JsonParams;
import com.cloud.exam.weboffice.entity.OpenFileParams;
import com.cloud.feign.file.FileClientFeign;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * WebOffice
 * @author 张争洋
 * @date 2020-11-06
 */
@Api(tags = "WebOffice")
@RestController
@RequestMapping("/weboffice")
@Slf4j
public class WebOfficeController {

    @Autowired
    private WebOfficeService webOfficeService;
//    @Autowired
//    private WebOfficeClient webOfficeClient;
    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;
    @Autowired
    private FileClientFeign fileClientFeign;

    Logger logger = LoggerFactory.getLogger(WebOfficeController.class);

    /**
     * 获取在线编辑office的URL
     *
     */
    @ApiOperation("获取在线编辑office的URL")
    @PostMapping("/open")
    public Map open(@RequestBody OpenFileParams openFileParams) {
        logger.info("参数 --> openFileParams:{}", openFileParams);
        return webOfficeService.open(openFileParams);
    }


    @ApiOperation("保存文档")
    @PostMapping("/save")
    public Map save(@RequestBody JsonParams jsonParams){
        Map<String,String> webOfficeObjectResult = webOfficeService.save(jsonParams);
        return webOfficeObjectResult;
    }


    /**
     * 上传文档模板到weboffice的指定路径下（服务器文件上传路径，也可以使用fastdfs的挂载路径）
     * 模板的保存路径为活动id_试卷idMap<String,String>
     * @return
     */
//    @ApiOperation("情报分析试题上传模板")
//    @PostMapping("/upload")
//    public void upload(@RequestPart String filePath,@RequestParam MultipartFile file) throws Exception {
//        @RequestParam("filePath") String filePath, @RequestPart MultipartFile file
//        Map<String,Object> map = new HashMap<>();
//        map.put("filePath",File.separator);
//        map.put("file",file);
//        FileSystemResource fileSystemResource  =new FileSystemResource(filePath,file);
//        String aa = webOfficeClient.uploadFile(file);
//        System.out.println(aa);
//        Map<String,String> webOfficeObjectResult= webOfficeService.upload(file);
//        return webOfficeObjectResult;
//    }

    /**
     * 上传情报分析模板并将其存于weboffice可以访问的目录下，返回路径给前端
     * @param multipartFile
     * @return
     */
    @ApiOperation("情报分析试题上传模板")
    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam("file") MultipartFile multipartFile,@RequestParam("md5") String fileMd5){
        String uploadPath = configurableApplicationContext.getEnvironment().getProperty("weboffice.dir");
        File file = new File(uploadPath+File.separator+fileMd5+multipartFile.getOriginalFilename());
        if (file.exists()){
            return file.getAbsolutePath();
        }
        if (!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        try {
            file.createNewFile();
            OutputStream os = new FileOutputStream(file);
            os.write(multipartFile.getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }
        Map<String,Object> map = new HashMap<>();
        map.put("id",fileMd5);
        map.put("name",multipartFile.getOriginalFilename());
        map.put("url",file.getAbsolutePath());
        fileClientFeign.saveFile(map);
        return file.getAbsolutePath();
    }
}
