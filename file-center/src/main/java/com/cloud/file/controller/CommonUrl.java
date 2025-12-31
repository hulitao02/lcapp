package com.cloud.file.controller;

/**
 * @author:胡立涛
 * @description: TODO 影像切片，发布地址
 * @date: 2022/4/21
 * @param:
 * @return:
 */
public class CommonUrl {
    // 创建切片任务_选择数据 get
    public static String taskInfo = "/taskmanager/api/v1/task/taskInfo?inputEpsgCode=4326&outputEpsgCode=4326";
    // 创建切片_设置参数 post
    public static String task = "/taskmanager/api/v1/task";
    // 发布服务_设置参数 get
    public static String metadata = "/tilecache/api/v1/layers/mbtiles/metadata";
    // 发布服务_发布 post
    public static String mbtiles = "/tilecache/api/v1/layers/mbtiles";
}
