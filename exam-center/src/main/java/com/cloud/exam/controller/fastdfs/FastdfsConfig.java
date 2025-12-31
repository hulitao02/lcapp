package com.cloud.exam.controller.fastdfs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * fastdfs 相关的配置项
 */
@Configuration
public class FastdfsConfig {
    /**
     * 文件输出目录
     */
    @Value("${file.download.dir}")
    public String DIR;
    /**
     *  输出文件名称前缀
     */
    public final static String TARGET_FOLDER_PREFIX = "QUESTIONS_";
    /**
     *  文件服务器 group1 前缀
     *  M00
     */
    public final static String FASTDFS_GROUP = "group1";
    public final static String FASTDFS_M00 = "M00";




}
