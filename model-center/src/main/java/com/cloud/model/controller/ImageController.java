package com.cloud.model.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.model.dao.ImageDao;
import com.cloud.model.model.Image;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author:胡立涛
 * @description: TODO 舰面符号
 * @date: 2024/8/16
 * @param:
 * @return:
 */
@RestController
@RequestMapping("/image")
@Slf4j
@RefreshScope
public class ImageController {

    // 文件服务地址
    @Value(value = "${file_server}")
    private String fileServer;
    @Autowired
    ImageDao imageDao;

    /**
     * @author:胡立涛
     * @description: TODO 添加舰载符号
     * @date: 2024/8/16
     * @param: [image]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "add")
    public ApiResult add(@RequestBody Image image) {
        try {
            imageDao.insert(image);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据id查询详情
     * @date: 2024/8/16
     * @param: [image]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "detail")
    public ApiResult detail(@RequestBody Image image) {
        try {
            Image detail = imageDao.selectById(image.getId());
            detail.setFileServer(fileServer);
            return ApiResultHandler.buildApiResult(200, "操作成功", detail);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据id更新信息
     * @date: 2024/8/16
     * @param: [image]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "update")
    public ApiResult update(@RequestBody Image image) {
        try {
            imageDao.updateById(image);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据id删除图片
     * @date: 2024/8/16
     * @param: [image]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "del")
    public ApiResult del(@RequestBody Image image) {
        try {
            imageDao.deleteById(image.getId());
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 舰面符号列表
     * @date: 2024/8/16
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "list")
    public ApiResult list(@RequestBody Map map) {
        try {
            int pageSize = Integer.parseInt(map.get("pageSize").toString());
            int pageNo = Integer.parseInt(map.get("pageNo").toString());
            Page<Image> page = new Page();
            page.setCurrent(pageNo);
            page.setSize(pageSize);
            QueryWrapper<Image> queryWrapper = new QueryWrapper<>();
            Page<Image> imagePage = imageDao.selectPage(page, queryWrapper);
            Map rMap = new HashMap();
            rMap.put("page", imagePage);
            rMap.put("fileServer", fileServer);
            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }
}
