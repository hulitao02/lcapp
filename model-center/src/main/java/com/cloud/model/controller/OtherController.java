package com.cloud.model.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.core.ResultMesEnum;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.model.dao.ModelKpDao;
import com.cloud.model.model.ModelKp;
import io.swagger.annotations.ApiModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/other")
@ApiModel(value = "对外提供接口")
@Slf4j
@RefreshScope
public class OtherController {

    @Autowired
    ManageBackendFeign manageBackendFeign;
    @Autowired
    ModelKpDao modelKpDao;

    /**
     * @author:胡立涛
     * @description: TODO 根据当前知识点，获取下一级知识点信息
     * @date: 2022/6/28
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getNextPoint")
    public ApiResult getNextPoint(@RequestBody Map<String, Object> map) {
        try {
            List<Map> nextPoint = manageBackendFeign.getNextPoint(map);
            if (nextPoint != null && nextPoint.size() > 0) {
                for (Map bean : nextPoint) {
                    map.put("kpId", Long.valueOf(bean.get("id").toString()));
                    List<Map> count = modelKpDao.getCount(map);
                    int size = count == null ? 0 : count.size();
                    bean.put("modelCount", size);
                }
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", nextPoint);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据知识点名称关键字，返回知识点ID的集合
     * @date: 2022/6/28
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getPointListForName")
    public ApiResult getPointListForName(@RequestBody Map<String, Object> map) {
        try {
            List<Map> pointListForName = manageBackendFeign.getPointListForName(map);
            return ApiResultHandler.buildApiResult(200, "操作成功", pointListForName);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据当前知识点，获取下一级知识点信息(已完成数据对接的）
     * @date: 2022/6/28
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getNextPointData")
    public ApiResult getNextPointData(@RequestBody Map<String, Object> map) {
        try {
            List<Map> rList = new ArrayList<>();
            String pointName = map.get("pointName") == null ? null : map.get("pointName").toString();
            if (!pointName.isEmpty()) {
                pointName = "%" + pointName + "%";
                map.put("pointName", pointName);
            }
            List<Map> nextPoint = manageBackendFeign.getNextPoint(map);
            if (nextPoint != null && nextPoint.size() > 0) {
                for (Map bean : nextPoint) {
                    Long kpId = Long.valueOf(bean.get("id").toString());
                    // 判断该知识点是否完成数据对接
                    QueryWrapper<ModelKp> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("status", 2);
                    queryWrapper.eq("kp_id", kpId);
                    List<ModelKp> modelKps = modelKpDao.selectList(queryWrapper);
                    if (!modelKps.isEmpty() && modelKps.size() > 0) {
                        map.put("kpId", Long.valueOf(bean.get("id").toString()));
                        List<Map> count = modelKpDao.getCount(map);
                        int size = count == null ? 0 : count.size();
                        bean.put("modelCount", size);
                        rList.add(bean);
                    }
                }
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", rList);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


}
