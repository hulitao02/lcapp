package com.cloud.model.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.feign.exam.ExamFeign;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.model.common.KnowledgePoints;
import com.cloud.model.dao.KnowledgeProDao;
import com.cloud.model.dao.ModelDataDao;
import com.cloud.model.dao.ModelKpDao;
import com.cloud.model.model.KnowledgePro;
import com.cloud.model.service.DataService;
import com.cloud.model.service.ModelKpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author:胡立涛
 * @description: TODO 数据同步
 * @date: 2021/12/6
 * @param:
 * @return:
 */
@RestController
@RequestMapping("/data")
@Slf4j
@RefreshScope
public class DataController {

    // 文件服务地址
    @Value(value = "${file_server}")
    private String fileServer;
    @Autowired
    private ManageBackendFeign manageBackendFeign;
    @Autowired
    ModelKpDao modelKpDao;
    @Autowired
    ModelDataDao modelDataDao;
    @Autowired
    KnowledgeProDao knowledgeProDao;
    @Autowired
    DataService dataService;
    @Autowired
    ModelKpService modelKpService;
    @Autowired
    ExamFeign examFeign;

    /**
     * @author:胡立涛
     * @description: TODO 验证知识点是否已使用
     * @date: 2022/1/25
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "checkClassData")
    public ApiResult checkClassData(@RequestBody Map<String, Object> map) {
        try {
            if (StringUtils.isEmpty(map.get("kpCode"))) {
                return ApiResultHandler.buildApiResult(100, "参数kpCode为空", null);
            }
            // 根据kpCode查询 kpId
            Map kpMap = manageBackendFeign.getKnowledgePointsByCode(map.get("kpCode").toString());
            long kpId = Long.valueOf(kpMap.get("id").toString());
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("kp_id", kpId);
            queryWrapper.eq("status", 2);
            List list = modelKpDao.selectList(queryWrapper);
            if (list != null && list.size() > 0) {
                return ApiResultHandler.buildApiResult(200, "知识点" + kpId + "已被使用", 1);
            }
            // 查看该知识点是否已绑定试题
            int checkKpId = examFeign.checkKpId(kpId);
            if (checkKpId == 1) {
                return ApiResultHandler.buildApiResult(200, "知识点" + kpId + "已被使用", 1);
            }
            KnowledgePoints knowledgePoints = new KnowledgePoints();
            knowledgePoints.setParentId(kpId);
            List<Map> aClass = manageBackendFeign.getClass(knowledgePoints);
            if (!StringUtils.isEmpty(aClass)) {
                for (Map m : aClass) {
                    queryWrapper = new QueryWrapper();
                    queryWrapper.eq("kp_id", Long.valueOf(m.get("id").toString()));
                    queryWrapper.eq("status", 2);
                    list = modelKpDao.selectList(queryWrapper);
                    if (list != null && list.size() > 0) {
                        return ApiResultHandler.buildApiResult(200, "知识点" + m.get("id") + "已被使用", 1);
                    }
                    checkKpId = examFeign.checkKpId(Long.valueOf(m.get("id").toString()));
                    if (checkKpId == 1) {
                        return ApiResultHandler.buildApiResult(200, "知识点" + m.get("id") + "已被使用", 1);
                    }
                }
            }
            return ApiResultHandler.buildApiResult(200, "知识点未被使用", 0);
        } catch (Exception e) {
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 验证属性类别/属性是否已被使用
     * @date: 2021/12/7
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "checkPropertyData")
    public ApiResult checkPropertyData(@RequestBody Map<String, Object> map) {
        try {
            if (StringUtils.isEmpty(map.get("kpCode"))) {
                return ApiResultHandler.buildApiResult(100, "参数kpCode为空", null);
            }
            // 根据知识code查询知识点id
            Map kpMap = manageBackendFeign.getKnowledgePointsByCode(map.get("kpCode").toString());
            Long kpId = Long.valueOf(kpMap.get("id").toString());
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("kp_id", kpId);
            if (!StringUtils.isEmpty(map.get("proCode"))) {
                queryWrapper.eq("pro_proname", map.get("proCode").toString());
            }
            if (!StringUtils.isEmpty(map.get("proTypeCode"))) {
                queryWrapper.eq("pro_type_name", map.get("proTypeCode").toString());
            }
            List list = modelDataDao.selectList(queryWrapper);
            if (list != null && list.size() > 0) {
                return ApiResultHandler.buildApiResult(200, "已使用", 1);
            }
            return ApiResultHandler.buildApiResult(200, "未使用", 0);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 修改属性分类名称
     * @date: 2021/12/7
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "updateProTypeName")
    public ApiResult updateProTypeName(@RequestBody Map<String, Object> map) {
        try {
            if (StringUtils.isEmpty(map.get("proTypeCode"))) {
                return ApiResultHandler.buildApiResult(100, "参数proTypeCode为空", null);
            }
            if (StringUtils.isEmpty(map.get("proTypeName"))) {
                return ApiResultHandler.buildApiResult(100, "参数proTypeName为空", null);
            }
            knowledgeProDao.updateInfoByTypeCode(map);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 修改属性名称
     * @date: 2021/12/7
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "updateProName")
    public ApiResult updateProName(@RequestBody Map<String, Object> map) {
        try {
            if (StringUtils.isEmpty(map.get("proCode"))) {
                return ApiResultHandler.buildApiResult(100, "参数proCode为空", null);
            }
            if (StringUtils.isEmpty(map.get("proName"))) {
                return ApiResultHandler.buildApiResult(100, "参数proName为空", null);
            }
            knowledgeProDao.updateInfoByProCode(map);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 删除属性
     * @date: 2021/12/8
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "delProName")
    public ApiResult delProName(@RequestBody Map<String, Object> map) {
        try {
            if (StringUtils.isEmpty(map.get("proCode"))) {
                return ApiResultHandler.buildApiResult(100, "参数proCode为空", null);
            }
            dataService.delProName(map);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 删除属性分类
     * @date: 2021/12/8
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "delProTypeName")
    public ApiResult delProTypeName(@RequestBody Map<String, Object> map) {
        try {
            if (StringUtils.isEmpty(map.get("proTypeCode"))) {
                return ApiResultHandler.buildApiResult(100, "参数proTypeCode为空", null);
            }
            dataService.delProTypeName(map);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 添加属性
     * @date: 2021/12/8
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "addProName")
    public ApiResult addProName(@RequestBody Map<String, Object> map) {
        try {
            if (StringUtils.isEmpty(map.get("kpCode"))) {
                return ApiResultHandler.buildApiResult(100, "参数kpCode为空", null);
            }
            if (StringUtils.isEmpty(map.get("proTypeName"))) {
                return ApiResultHandler.buildApiResult(100, "参数proTypeName为空", null);
            }
            if (StringUtils.isEmpty(map.get("proName"))) {
                return ApiResultHandler.buildApiResult(100, "参数proName为空", null);
            }
            if (StringUtils.isEmpty(map.get("proCode"))) {
                return ApiResultHandler.buildApiResult(100, "参数proCode为空", null);
            }
            if (StringUtils.isEmpty(map.get("proTypeCode"))) {
                return ApiResultHandler.buildApiResult(100, "参数proTypeCode为空", null);
            }
            if (StringUtils.isEmpty(map.get("isProperty"))) {
                return ApiResultHandler.buildApiResult(100, "参数isProperty为空", null);
            }
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("kp_code", map.get("kpCode").toString());
            queryWrapper.eq("pro_code", map.get("proCode").toString());
            queryWrapper.eq("pro_type_code", map.get("proTypeCode").toString());
            Integer count = knowledgeProDao.selectCount(queryWrapper);
            if (count > 0) {
                return ApiResultHandler.buildApiResult(200, "已存在该属性", null);
            }
            KnowledgePro knowledgePro = new KnowledgePro();
            knowledgePro.setIsProperty(Integer.parseInt(map.get("isProperty").toString()));
            // 根据kpcode查询kpid
            Map kpCode = manageBackendFeign.getKnowledgePointsByCode(map.get("kpCode").toString());
            if (kpCode == null) {
                return ApiResultHandler.buildApiResult(200, "没有对应的知识点", null);
            }
            knowledgePro.setKpId(Long.valueOf(kpCode.get("id").toString()));
            knowledgePro.setKpCode(map.get("kpCode").toString());
            knowledgePro.setProProname(map.get("proName").toString());
            knowledgePro.setProTypeName(map.get("proTypeName").toString());
            knowledgePro.setProTypeCode(map.get("proTypeCode").toString());
            knowledgePro.setProCode(map.get("proCode").toString());
            knowledgePro.setUpdateTime(new Date());
            knowledgePro.setStatus(1);
            knowledgeProDao.insert(knowledgePro);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 添加知识点
     * @date: 2021/12/8
     * @param: [knowledgePoints]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "addClass")
    public ApiResult addClass(@RequestBody KnowledgePoints knowledgePoints) {
        try {
            if (StringUtils.isEmpty(knowledgePoints.getCode())) {
                return ApiResultHandler.buildApiResult(100, "参数code为空", null);
            }
            if (StringUtils.isEmpty(knowledgePoints.getPointName())) {
                return ApiResultHandler.buildApiResult(100, "参数pointName为空", null);
            }
            if (StringUtils.isEmpty(knowledgePoints.getParentCode())) {
                return ApiResultHandler.buildApiResult(100, "参数parentCode为空", null);
            }
            knowledgePoints.setParentId(0L);
            // 根据parentCode查询kpId
            if (!knowledgePoints.getParentCode().equals("0")) {
                Map knowledgePointsByCode = manageBackendFeign.getKnowledgePointsByCode(knowledgePoints.getParentCode());
                knowledgePoints.setParentId(Long.valueOf(knowledgePointsByCode.get("id").toString()));
            }
            String s = manageBackendFeign.saveInfo(knowledgePoints);
            if (s.equals("100")) {
                return ApiResultHandler.buildApiResult(100, "已存在该知识点", null);
            }
            if (s == null) {
                return ApiResultHandler.buildApiResult(500, "操作异常", null);
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 修改知识点名称
     * @date: 2021/12/8
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "updateClass")
    public ApiResult updateClass(@RequestBody KnowledgePoints knowledgePoints) {
        try {
            if (StringUtils.isEmpty(knowledgePoints.getCode())) {
                return ApiResultHandler.buildApiResult(100, "参数code为空", null);
            }
            if (StringUtils.isEmpty(knowledgePoints.getPointName())) {
                return ApiResultHandler.buildApiResult(100, "参数pointName为空", null);
            }
            String s = manageBackendFeign.updateClass(knowledgePoints);
            if (s == null) {
                return ApiResultHandler.buildApiResult(500, "操作异常", null);
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 删除知识点（删除知识点及下级知识点，知识点下的知识，知识点的属性，知识点与模板关系，知识点绑定的数据关系）
     * @date: 2021/12/8
     * @param: [knowledgePoints]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "delClass")
    public ApiResult delClass(@RequestBody KnowledgePoints knowledgePoints) {
        try {
            if (StringUtils.isEmpty(knowledgePoints.getCode())) {
                return ApiResultHandler.buildApiResult(100, "参数code为空", null);
            }
            Map knowledgePointsByCode = manageBackendFeign.getKnowledgePointsByCode(knowledgePoints.getCode());
            knowledgePoints.setId(Long.valueOf(knowledgePointsByCode.get("id").toString()));
            // 根据知识点id查询知识点下的知识点信息
            List<Map> list = manageBackendFeign.getClassById(knowledgePoints);
            // 删除知识点和知识点下的知识点
            String s = manageBackendFeign.delClass(knowledgePoints);
            if (s == null) {
                return ApiResultHandler.buildApiResult(500, "操作异常", null);
            }
            dataService.delClass(list);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 批量 知识点属性数据同步
     * @date: 2022/2/18
     * @param: [list]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "bathPro")
    public ApiResult bathPro(@RequestBody List<Map> list) {
        try {
            if (list == null || list.size() == 0) {
                return ApiResultHandler.buildApiResult(100, "参数为空", null);
            }
            dataService.bathPro(list);
            log.info(" bathPro 同步接口更新完成 ");
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 批量 知识点关系数据同步
     * @date: 2022/2/18
     * @param: [list]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "bathRelation")
    public ApiResult bathRelation(@RequestBody List<Map> list) {

        try {
            if (list == null || list.size() == 0) {
                return ApiResultHandler.buildApiResult(100, "参数为空", null);
            }
            dataService.bathRelation(list);
            log.info(" bathRelation 同步接口更新完成 ");
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("bathRelation 异常: " + e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }
}
