package com.cloud.exam.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.exam.dao.QuestionDao;
import com.cloud.exam.service.WebOfficeService;
import com.cloud.exam.utils.FileUtils;
import com.cloud.exam.weboffice.constant.MethodType;
import com.cloud.exam.weboffice.entity.JsonParams;
import com.cloud.exam.weboffice.entity.OpenFileParams;
import com.cloud.exam.weboffice.entity.WebOfficeObjectResult;
import com.cloud.exam.weboffice.feign.WebOfficeClient;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WebOfficeServiceImpl implements WebOfficeService {

    @Autowired
    private WebOfficeClient webOfficeClient;
    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;

    Logger logger = LoggerFactory.getLogger(WebOfficeServiceImpl.class);


    /**
     * 打开文档，没有则复制一份模板并打开，有则直接打开
     * 准考证id作为存储所有学员答案的文件夹，文件名称（fileId）是试题id_随机uuid
     * @param openFileParams
     * @return
     */
    @Override
    public Map<String,String> open(OpenFileParams openFileParams){
        Integer userRight = 0;
        String modelUrl = questionDao.selectById(openFileParams.getQuestionId()).getModelUrl();
        logger.info("-----------------create user modelurlDir-----------------"+modelUrl);
        String userId = "1";
        String fileName = modelUrl.substring(modelUrl.lastIndexOf("/"));
        String ext = fileName.substring(fileName.lastIndexOf("."));
        String copyNewName = openFileParams.getIdentityId()+openFileParams.getQuestionId()+ext;
        String userMenuPermission = "{\"yozo_WP_cooperaters\":0}";

        logger.info("-----------------copyNewName-----------------"+copyNewName);
        if (log.isDebugEnabled()) {
            log.debug(String.format("参数 --> openFileParams:{}", openFileParams));
        }
        String newFilePath = configurableApplicationContext.getEnvironment().getProperty("weboffice.dir");
        Map<String,String> dataMap = new HashMap<>(2);
        try {
            File newFile = new File(newFilePath+File.separator+copyNewName);
            if (!newFile.exists()) {
                File copyFile = new File(modelUrl);
                newFilePath = FileUtils.saveFileByType(copyFile, newFilePath, copyNewName);
            }
            openFileParams.setFilePath(newFilePath);
//            String result = webOfficeClient.openFile(rootPath, openFileParams.getIdentityId()+File.separator+fileName, openFileParams.isReadOnly(),userId, openFileParams.isSaveFlag());
            if (openFileParams.isReadOnly()){
                userRight = 1;
            }
            JsonParams jsonParams = new JsonParams(1,new JsonParams.Params(userId, openFileParams.getIdentityId()+openFileParams.getQuestionId(),userRight,copyNewName,openFileParams.isSaveFlag(),userMenuPermission));
            String result = webOfficeClient.api(newFilePath,jsonParams.toString());
            JSONObject jsonObject = JSONObject.parseObject(result);
            WebOfficeObjectResult webOfficeObjectResult = JSON.toJavaObject(jsonObject,WebOfficeObjectResult.class);
            if (!"0".equals(webOfficeObjectResult.getErrorCode())) {
                throw new IllegalArgumentException(webOfficeObjectResult.getErrorMessage());
            }
            dataMap.put("url", ((Map) webOfficeObjectResult.getResult()).get("urls").toString());/*
            dataMap.put("fileId", openFileParams.getFileId());*/
            dataMap.put("msg",webOfficeObjectResult.getErrorMessage());
        }catch (Exception e){
            e.printStackTrace();
        }
        return dataMap;
    }



    /**
     * 保存对应用户的考试内容，用户点击保存时并将保存到redis
     *
     * @param jsonParams
     * @return
     */
    @Override
    public Map<String, String> save(JsonParams jsonParams) {
        Map<String, String> dataMap = new HashMap<>(2);
        if (log.isDebugEnabled()) {
            log.debug(String.format("参数 --> openFileParams:{}", jsonParams));
        }
        String result = webOfficeClient.apiOpenFile(jsonParams.toString());
        JSONObject jsonObject = JSONObject.parseObject(result);
        WebOfficeObjectResult webOfficeObjectResult = JSON.toJavaObject(jsonObject,WebOfficeObjectResult.class);
        if (!"0".equals(webOfficeObjectResult.getErrorCode())) {
            throw new IllegalArgumentException(webOfficeObjectResult.getErrorMessage());
        }

        if (jsonParams.getMethod()== MethodType.OPEN_FILE) {
            dataMap.put("url", ((Map) webOfficeObjectResult.getResult()).get("urls").toString());
            dataMap.put("fileId", jsonParams.getParams().getFileId());
        }else if (jsonParams.getMethod()==MethodType.SAVE_FILE){
            dataMap.put("code", "0");
            dataMap.put("message", "保存成功！");
        }
        return dataMap;
    }

    @Override
    public Map<String, String> upload(MultipartFile multipartFile) {
        /*Map<String,String> map = new HashMap<>();
        String rootPath = configurableApplicationContext.getEnvironment().getProperty("weboffice.dir");
        String filePath = WebOfficeUtil.getNewFilePath(rootPath,multipartFile.getName());
        try{
        String result = webOfficeClient.uploadFile(filePath,multipartFile);
        JSONObject jsonObject = JSONObject.parseObject(result);
        WebOfficeObjectResult webOfficeObjectResult = JSON.toJavaObject(jsonObject,WebOfficeObjectResult.class);
        if (!"0".equals(webOfficeObjectResult.getErrorCode())) {
            System.out.println(webOfficeObjectResult+"..........");
            throw new IllegalArgumentException(webOfficeObjectResult.getErrorMessage());
        }
        //filePath为fileId+文件名称
        map.put("url",rootPath+"/"+((Map) webOfficeObjectResult.getResult()).get("filePath").toString());
        map.put("fileId",((Map) webOfficeObjectResult.getResult()).get("fileId").toString());
        }catch (Exception e){
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
        return map;

         */
        return null;
    }

}
