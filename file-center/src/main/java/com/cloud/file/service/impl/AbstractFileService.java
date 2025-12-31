package com.cloud.file.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.file.controller.CommonUrl;
import com.cloud.file.dao.FileDao;
import com.cloud.file.model.FileInfo;
import com.cloud.file.model.FileSource;
import com.cloud.file.service.FileService;
import com.cloud.file.utils.CommonUtil;
import com.cloud.file.utils.Constants;
import com.cloud.file.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
public abstract class AbstractFileService implements FileService {

    // 启的影像服务地址
    @Value("${tifServerUrl}")
    private String tifServerUrl;
    @Value("${inputGridSet}")
    private String inputGridSet;
    @Value("${outputGridSet}")
    private String outputGridSet;
    // 切片存放地址
    @Value("${savePath}")
    private String savePath;
    @Value("${taskType}")
    private String taskType;

    protected abstract FileDao getFileDao();

    @Override
    public FileInfo upload(MultipartFile file, Boolean flag) throws Exception {
        FileInfo fileInfo = FileUtil.getFileInfo(file);
        // 先根据文件md5查询记录
        FileInfo oldFileInfo = getFileDao().getById(fileInfo.getId());

        if (oldFileInfo != null) {// 如果已存在文件，避免重复上传同一个文件
            return oldFileInfo;
        }

        if (!fileInfo.getName().contains(".")) {
            throw new IllegalArgumentException("缺少后缀名");
        }

        if (flag) {
            uploadFile(file, fileInfo);
            fileInfo.setSource(fileSource().name());// 设置文件来源
            getFileDao().save(fileInfo);// 将文件信息保存到数据库
        } else {
            uploadFileByName(file, fileInfo);//保留文件名称上传
        }

        log.info("上传文件：{}", fileInfo);

        return fileInfo;
    }

    /**
     * 文件来源
     *
     * @return
     */
    protected abstract FileSource fileSource();

    /**
     * 上传文件
     *
     * @param file
     * @param fileInfo
     */
    protected abstract void uploadFile(MultipartFile file, FileInfo fileInfo) throws Exception;

    /**
     * 上传文件(自定义文件名称)
     *
     * @param file
     * @param fileInfo
     */
    protected abstract void uploadFileByName(MultipartFile file, FileInfo fileInfo) throws Exception;

    @Override
    public void delete(FileInfo fileInfo) {
//		deleteFile(fileInfo);
        getFileDao().delete(fileInfo.getId());
        log.info("删除文件：{}", fileInfo);
    }

    /**
     * 删除文件资源
     *
     * @param fileInfo
     * @return
     */
    protected abstract boolean deleteFile(FileInfo fileInfo);


    /**
     * 查找文件是否存在
     *
     * @param id
     * @return
     */
    @Override
    public FileInfo getById(String id) {
        return getFileDao().getById(id);
    }

    /**
     * 存储文件到数据库中
     *
     * @param fileInfo
     * @return
     */
    @Override
    public int save(FileInfo fileInfo) {
        fileInfo.setCreateTime(new Date());
        return getFileDao().save(fileInfo);
    }


    public int deleteById(String id) {
        return getFileDao().delete(id);
    }


    public int updateFile(FileInfo fileInfo) {
        return getFileDao().updateFile(fileInfo);
    }


    /**
     * @author:胡立涛
     * @description: TODO 影像切片，发布处理逻辑
     * @date: 2022/4/21
     * @param: []
     * @return: int
     */
    @Transactional
    @Override
    public int tifUpload(String path, String fileName) throws Exception {
        // 第一步 创建切片任务_选择数据
        String url = tifServerUrl + CommonUrl.taskInfo;
        JSONObject json = new JSONObject();
        json.put("path", path);
        log.info("调用切片服务，请求地址:{}, 入参: {} ", url, json);
        JSONObject jsonObject = CommonUtil.esMethod(url, json);
        JSONObject data = jsonObject.get("data") == null ? null : (JSONObject) jsonObject.get("data");
        if (data == null) {
            return -1;
        }
        int minLevel = data.get("minLevel") == null ? -1 : data.getInteger("minLevel");
        int maxLevel = data.get("maxLevel") == null ? -1 : data.getInteger("maxLevel");
        if (minLevel == -1) {
            return -1;
        }
        if (maxLevel == -1) {
            return -1;
        }
        log.info("------第一步返回结果:" + jsonObject.toJSONString());
        // 第二步 创建切片_设置参数
        url = tifServerUrl + CommonUrl.task;
        json = new JSONObject();
        json.put("encrypt", false);
        json.put("grayStretching", false);
        json.put("inputGridSet", inputGridSet);
        json.put("inputPath", path);
        json.put("maxLevel", maxLevel);
        json.put("mimeType", "png");
        json.put("minLevel", minLevel);
        json.put("outputGridSet", outputGridSet);
        json.put("rgbIndex", "1,2,3");
        json.put("savePath", savePath);
        json.put("saveType", "mbtiles");
        String taskName = fileName.substring(0, fileName.indexOf(".")).replace("-", "") + "-" + Constants.id.incrementAndGet();
        json.put("taskName", taskName);
        json.put("taskType", taskType);
        json.put("transparentColor", "");
        jsonObject = CommonUtil.esMethod(url, json);
        data = jsonObject.get("data") == null ? null : (JSONObject) jsonObject.get("data");
        if (data == null) {
            return -1;
        }
        log.info("------第二步返回结果-:" + JSON.toJSONString(data));
        String outPath = data.get("savePath") == null ? null : data.getString("savePath");
        if (outPath == null) {
            return -1;
        }
        // 第三步 发布服务_设置参数
        url = tifServerUrl + CommonUrl.metadata;
        //超时时间暂定1分钟
        long timeout = 10000L;
        long startTime = System.currentTimeMillis();
        jsonObject = null;
        //循环请求，大概每隔3秒，一直到请求到预期结果或者超时
        while (jsonObject == null && System.currentTimeMillis() - startTime < timeout) {
            Thread.sleep(3000);
            jsonObject = CommonUtil.esGetMethod(url, outPath);
        }
        for (int i = 0; i < 2; i++) {
            if (jsonObject == null) {
                Thread.sleep(5000);
                jsonObject = getObject(path, fileName);
            }
        }
        log.info("------第三步返回结果：{}", jsonObject);
        BigDecimal minX = jsonObject.get("minX") == null ? null : jsonObject.getBigDecimal("minX");
        if (minX == null) {
            return -1;
        }
        BigDecimal minY = jsonObject.get("minY") == null ? null : jsonObject.getBigDecimal("minY");
        if (minY == null) {
            return -1;
        }
        BigDecimal maxX = jsonObject.get("maxX") == null ? null : jsonObject.getBigDecimal("maxX");
        if (maxX == null) {
            return -1;
        }
        BigDecimal maxY = jsonObject.get("maxY") == null ? null : jsonObject.getBigDecimal("maxY");
        if (maxY == null) {
            return -1;
        }
        // 第四步 发布服务_发布
        url = tifServerUrl + CommonUrl.mbtiles;
        json = new JSONObject();
        json.put("dataEntityID", "");
        json.put("dmFilePath", outPath);
        json.put("fromDM", false);
        json.put("gridSet", "EPSG:3857");
        json.put("isBase", true);
        json.put("layerName", taskName + "-PNG-3857");
        json.put("maxLevel", maxLevel);
        json.put("minX", minX);
        json.put("minY", minY);
        json.put("maxX", maxX);
        json.put("maxY", maxY);
        json.put("mimeType", "PNG");
        json.put("minLevel", minLevel);
        json.put("path", outPath);
        jsonObject = CommonUtil.esMethod(url, json);
        log.info("------第四步返回结果：" + JSON.toJSONString(jsonObject));
        int id = jsonObject.get("id") == null ? -1 : jsonObject.getInteger("id");
        log.info("----id的值：" + id);
        return id;
    }

    private JSONObject getObject(String path, String fileName) throws Exception {
        // 第一步 创建切片任务_选择数据
        String url = tifServerUrl + CommonUrl.taskInfo;
        JSONObject json = new JSONObject();
        json.put("path", path);
        log.info("调用切片服务，请求地址:{}, 入参: {} ", url, json);
        JSONObject jsonObject = CommonUtil.esMethod(url, json);
        JSONObject data = jsonObject.get("data") == null ? null : (JSONObject) jsonObject.get("data");
        if (data == null) {
            return null;
        }
        int minLevel = data.get("minLevel") == null ? -1 : data.getInteger("minLevel");
        int maxLevel = data.get("maxLevel") == null ? -1 : data.getInteger("maxLevel");
        if (minLevel == -1) {
            return null;
        }
        if (maxLevel == -1) {
            return null;
        }
        log.info("------第一步返回结果:" + jsonObject.toJSONString());
        // 第二步 创建切片_设置参数
        url = tifServerUrl + CommonUrl.task;
        json = new JSONObject();
        json.put("encrypt", false);
        json.put("grayStretching", false);
        json.put("inputGridSet", inputGridSet);
        json.put("inputPath", path);
        json.put("maxLevel", maxLevel);
        json.put("mimeType", "png");
        json.put("minLevel", minLevel);
        json.put("outputGridSet", outputGridSet);
        json.put("rgbIndex", "1,2,3");
        json.put("savePath", savePath);
        json.put("saveType", "mbtiles");
        String taskName = fileName.substring(0, fileName.indexOf(".")).replace("-", "");
        json.put("taskName", taskName);
        json.put("taskType", taskType);
        json.put("transparentColor", "");
        jsonObject = CommonUtil.esMethod(url, json);
        data = jsonObject.get("data") == null ? null : (JSONObject) jsonObject.get("data");
        if (data == null) {
            return null;
        }
        log.info("------第二步返回结果-:" + JSON.toJSONString(data));
        String outPath = data.get("savePath") == null ? null : data.getString("savePath");
        if (outPath == null) {
            return null;
        }
        // 第三步 发布服务_设置参数
        url = tifServerUrl + CommonUrl.metadata;
        //超时时间暂定1分钟
        long timeout = 10000L;
        long startTime = System.currentTimeMillis();
        jsonObject = null;
        //循环请求，大概每隔3秒，一直到请求到预期结果或者超时
        while (jsonObject == null && System.currentTimeMillis() - startTime < timeout) {
            Thread.sleep(3000);
            jsonObject = CommonUtil.esGetMethod(url, outPath);
        }
        return jsonObject;
    }

}
