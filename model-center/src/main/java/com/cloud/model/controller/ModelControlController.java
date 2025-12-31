package com.cloud.model.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.model.dao.ModelControlDao;
import com.cloud.model.model.KnowledgePic;
import com.cloud.model.model.ModelControl;
import com.cloud.model.service.ModelControlService;
import com.cloud.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/modelcontrol")
@ApiModel(value = "复杂模板右侧子项信息")
@Slf4j
@RefreshScope
public class ModelControlController {

    @Autowired
    ModelControlDao modelControlDao;

    @Autowired
    ModelControlService modelControlService;


    /**
     * @author:胡立涛
     * @description: TODO 复杂知识模板右侧子项添加逻辑
     * @date: 2022/9/22
     * @param: [modelControl]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "saveInfoOne")
    public ApiResult saveInfoOne(@RequestBody ModelControl modelControl) {
        try {
            if (modelControl.getModelKpId() == null) {
                return ApiResultHandler.buildApiResult(200, "参数modelKpId为空", null);
            }
            if (modelControl.getName() == null) {
                return ApiResultHandler.buildApiResult(200, "参数name为空", null);
            }
            if (modelControl.getType() == null) {
                return ApiResultHandler.buildApiResult(200, "参数flg为空", null);
            }
            modelControl.setCreateTime(new Timestamp(System.currentTimeMillis()));
            modelControl.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            modelControlDao.insert(modelControl);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 复杂模板 修改子项顺序逻辑
     * @date: 2022/9/22
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "saveInfoList")
    public ApiResult saveInfoList(@RequestBody Map<String, Object> map) {
        try {
            if (map.get("infoList") == null) {
                return ApiResultHandler.buildApiResult(100, "参数infoList为空", null);
            }
            // 根据id更新信息
            Integer integer = modelControlService.saveInfoList(map);
            if (integer == 101) {
                return ApiResultHandler.buildApiResult(100, "参数id为空", null);
            }
            if (integer == 102) {
                return ApiResultHandler.buildApiResult(100, "参数name为空", null);
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 复杂模板右侧子项列表信息查询
     * @date: 2022/9/22
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getList")
    public ApiResult getList(@RequestBody Map<String, Object> map) {
        try {
            if (map.get("modelKpId") == null) {
                return ApiResultHandler.buildApiResult(100, "参数modelKpId为空", null);
            }
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("model_kp_id", Long.valueOf(map.get("modelKpId").toString()));
            queryWrapper.orderByAsc("update_time");
            List list = modelControlDao.selectList(queryWrapper);
            return ApiResultHandler.buildApiResult(200, "操作成功", list);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }
}
