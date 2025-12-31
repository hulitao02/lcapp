package com.cloud.model.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.model.dao.ImageDao;
import com.cloud.model.dao.YcImageDao;
import com.cloud.model.model.Image;
import com.cloud.model.model.YcImage;
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
 * @description: TODO 烟囱形状、舰艏形状、水平舵
 * @date: 2024/8/16
 * @param:
 * @return:
 */
@RestController
@RequestMapping("/ycimage")
@Slf4j
@RefreshScope
public class YcImageController {

    // 文件服务地址
    @Value(value = "${file_server}")
    private String fileServer;
    @Autowired
    YcImageDao imageDao;

    /**
     * @author:胡立涛
     * @description: TODO 添加
     * @date: 2024/8/16
     * @param: [image]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "add")
    public ApiResult add(@RequestBody YcImage image) {
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
    public ApiResult detail(@RequestBody YcImage image) {
        try {
            YcImage detail = imageDao.selectById(image.getId());
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
    public ApiResult update(@RequestBody YcImage image) {
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
    public ApiResult del(@RequestBody YcImage image) {
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
     * @description: TODO 图片列表
     * @date: 2024/8/16
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "list")
    public ApiResult list(@RequestBody Map map) {
        try {
            int pageSize = Integer.parseInt(map.get("pageSize").toString());
            int pageNo = Integer.parseInt(map.get("pageNo").toString());
            int type = Integer.parseInt(map.get("type").toString());
            Page<YcImage> page = new Page();
            page.setCurrent(pageNo);
            page.setSize(pageSize);
            QueryWrapper<YcImage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("type", type);
            Page<YcImage> imagePage = imageDao.selectPage(page, queryWrapper);
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
