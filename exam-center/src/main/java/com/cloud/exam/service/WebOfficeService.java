package com.cloud.exam.service;

import com.cloud.exam.weboffice.entity.JsonParams;
import com.cloud.exam.weboffice.entity.OpenFileParams;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 会商授课
 *
 * @author 张争洋
 * @date 2020-11-06
 */
public interface WebOfficeService {



    /**
     * 获取在线编辑office的URL
     */
    Map<String,String> open(OpenFileParams openFileParams);

    /**
     * 获取在线编辑office的URL
     */
    Map<String, String> save(JsonParams jsonParams);

    Map<String,String> upload(MultipartFile multipartFile);







}

