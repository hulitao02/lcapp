package com.cloud.model.controller;


import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.feign.exam.ExamFeign;
import com.cloud.feign.knowledge.KnowledgeFeign;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.model.bean.vo.ModelKpVo;
import com.cloud.model.common.KnowledgePoints;
import com.cloud.model.dao.*;
import com.cloud.model.model.ModelData;
import com.cloud.model.service.DataService;
import com.cloud.model.service.KnowledgeViewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/delproperty")
@ApiModel(value = "删除属性controller")
@Slf4j
@RefreshScope
public class DelProController {
    @Autowired
    ManageBackendFeign manageBackendFeign;
    @Autowired
    ModelKpDao modelKpDao;
    @Autowired
    ModelDataDao modelDataDao;
    @Autowired
    DataService dataService;
    @Autowired
    KnowledgeFeign knowledgeFeign;


    /**
     * @author:胡立涛
     * @description: TODO 知识图谱：删除属性时验证逻辑及删除逻辑
     * @date: 2022/8/8
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "delPro")
    public ApiResult delPro(@RequestBody Map map) {
        try {
            Map rMap = new HashMap();
            List<Map> rList = new ArrayList<>();
            String proCode = map.get("proCode") == null ? null : map.get("proCode").toString();
            String kpCode = map.get("kpCode") == null ? null : map.get("kpCode").toString();
            if (proCode == null) {
                return ApiResultHandler.buildApiResult(100, "参数proCode为空", null);
            }
            // 情况一：知识点code为空，查看属性是否被使用
            // 若被使用，返回使用信息
            // 若未被使用，删除关联信息
            if (kpCode == null) {
                QueryWrapper<ModelData> queryWrapper = new QueryWrapper();
                queryWrapper.eq("pro_proname", proCode);
                List<ModelData> list = modelDataDao.selectList(queryWrapper);
                // 属性被使用
                if (!list.isEmpty() && list.size() > 0) {
                    for (ModelData modelData : list) {
                        // 使用该属性知识点code
                        Map pointBean = knowledgeFeign.getKnowledgePointsById(modelData.getKpId());
                        rMap.put("kpCode", pointBean.get("id"));
                        rMap.put("kpName", pointBean.get("name"));
                        // 使用该属性的模板名称
                        ModelKpVo modelKp = modelKpDao.getModelKpVoById(modelData.getModelKpId());
                        if (modelKp == null) {
                            continue;
                        }
                        rMap.put("modelId", modelKp.getId());
                        rMap.put("modelName", modelKp.getName());
                        rList.add(rMap);
                    }
                    return ApiResultHandler.buildApiResult(201, "已被使用", rList);
                }
                // 属性未被使用，走删除逻辑
                // 删除model_data表中的数据，删除knowledge_pro表中的数据
                map = new HashMap();
                map.put("proCode", proCode);
                dataService.delProName(map);
                return ApiResultHandler.buildApiResult(200, "删除成功", null);
            }
            // 情况二：知识点code不为空
            // 验证该知识点是否为当前根节点下的知识点：不是当前根节点下的知识点，直接删除
            // 为当前根节点下的知识点：根据知识点code，和属性code查看是否被使用 1>未被使用，删除 2>使用，不能删除
            Map knowledgePointsByCode = manageBackendFeign.getKnowledgePointsByCode(kpCode);
            if (knowledgePointsByCode != null) {
                // 查询根节点下的知识点
                Map rootPoint = manageBackendFeign.getRootPoint();
                Long rootId = rootPoint == null ? 41L : Long.valueOf(rootPoint.get("id").toString());
                List<Map> classById = manageBackendFeign.getClassByIdGet(rootId);
                Map rootMap = new HashMap();
                if (!classById.isEmpty() && classById.size() > 0) {
                    for (Map classMap : classById) {
                        rootMap.put(classMap.get("id").toString(), 1);
                    }
                }
                // 该知识点为根节点下的数据
                if (rootMap.get(String.valueOf(knowledgePointsByCode.get("id"))) != null) {
                    // 根据kpid和procode查看是否被使用
                    QueryWrapper<ModelData> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("kp_id", Long.valueOf(knowledgePointsByCode.get("id").toString()));
                    queryWrapper.eq("pro_proname", proCode);
                    List<ModelData> modelDataList = modelDataDao.selectList(queryWrapper);
                    if (modelDataList != null && modelDataList.size() > 0) {
                        for (ModelData modelData : modelDataList) {
                            // 属性被使用
                            rMap.put("kpCode", kpCode);
                            rMap.put("kpName", knowledgePointsByCode.get("pointname"));
                            ModelKpVo modelKpVoById = modelKpDao.getModelKpVoById(modelData.getModelKpId());
                            rMap.put("modelId", modelKpVoById.getId());
                            rMap.put("modelName", modelKpVoById.getName());
                            rList.add(rMap);
                        }
                    }
                }
            }
            if (rList.size() > 0) {
                return ApiResultHandler.buildApiResult(201, "已被使用", rList);
            }
            // 该知识点code在数据库中不存在 ｜｜ 该知识点不数据当前根节点下的知识点｜｜ 知识点未被使用均走删除逻辑
            map = new HashMap();
            map.put("kpId", Long.valueOf(knowledgePointsByCode.get("id").toString()));
            map.put("proCode", proCode);
            dataService.delProNameAndKpCode(map);
            return ApiResultHandler.buildApiResult(200, "删除成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


//    /**
//     * @author:胡立涛
//     * @description: TODO 知识图谱：删除关系时验证逻辑及删除逻辑
//     * @date: 2022/8/8
//     * @param: [map]
//     * @return: com.cloud.core.ApiResult
//     */
//    @PostMapping(value = "delRelation")
//    public ApiResult delRelation(@RequestBody Map map) {
//        try {
//            Map rMap = new HashMap();
//            List<Map> rList = new ArrayList<>();
//            String kpCode = map.get("kpCode") == null ? null : map.get("kpCode").toString();
//            String proCode = map.get("proCode") == null ? null : map.get("proCode").toString();
//            String proTypeCode = map.get("proTypeCode") == null ? null : map.get("proTypeCode").toString();
//            if (proTypeCode == null) {
//                return ApiResultHandler.buildApiResult(100, "参数proCode为空", null);
//            }
//            // 查询根节点下的知识点
//            Map rootPoint = manageBackendFeign.getRootPoint();
//            Long rootId = rootPoint == null ? 41L : Long.valueOf(rootPoint.get("id").toString());
//            List<Map> classById = manageBackendFeign.getClassByIdGet(rootId);
//            Map rootMap = new HashMap();
//            if (!classById.isEmpty() && classById.size() > 0) {
//                for (Map classMap : classById) {
//                    rootMap.put(classMap.get("id").toString(), 1);
//                }
//            }
//            // 情况一：知识点code，关系code，关系关联概念code不为空
//            // 查询知识点code是否为当前根节点下的知识点code，如果为不是当前根节点下的知识点code，走删除逻辑
//            // 如果为当前根节点下的知识点：根据知识点，关系code和关联概念code查询是否被使用
//            // 若被使用：不能删除
//            // 若未被使用：走删除逻辑
//            if (kpCode != null && proCode != null && proTypeCode != null) {
//                Map knowledgePointsByCode = manageBackendFeign.getKnowledgePointsByCode(kpCode);
//                if (knowledgePointsByCode != null) {
//                    // 该知识点为根节点下的知识点
//                    if (rootMap.get(String.valueOf(knowledgePointsByCode.get("id"))) != null) {
//                        QueryWrapper<ModelData> queryWrapper = new QueryWrapper<>();
//                        queryWrapper.eq("kp_id", Long.valueOf(knowledgePointsByCode.get("id").toString()));
//                        queryWrapper.eq("pro_proname", proCode);
//                        queryWrapper.eq("pro_type_name", proTypeCode);
//                        List<ModelData> modelDataList = modelDataDao.selectList(queryWrapper);
//                        // 已使用
//                        if (modelDataList != null && modelDataList.size() > 0) {
//                            for (ModelData modelData : modelDataList) {
//                                // 属性被使用
//                                rMap.put("kpCode", kpCode);
//                                rMap.put("kpName", knowledgePointsByCode.get("pointname"));
//                                ModelKpVo modelKpVoById = modelKpDao.getModelKpVoById(modelData.getModelKpId());
//                                if (modelKpVoById == null) {
//                                    continue;
//                                }
//                                rMap.put("modelId", modelKpVoById.getId());
//                                rMap.put("modelName", modelKpVoById.getName());
//                                rList.add(rMap);
//                            }
//                            return ApiResultHandler.buildApiResult(201, "已被使用", rList);
//                        }
//                    }
//                }
//                // 当前知识点在数据库中不存在 ｜｜ 未被使用 ｜｜ 知识点不是当前根节点下的知识点均走删除逻辑
//                map = new HashMap();
//                map.put("proCode", proCode);
//                map.put("kpId", Long.valueOf(knowledgePointsByCode.get("id").toString()));
//                map.put("proTypeCode", proTypeCode);
//                dataService.delProNameAndKpCode(map);
//                return ApiResultHandler.buildApiResult(200, "删除成功", null);
//            }
//            // 情况二：根据关系code删除
//            // 根据关系code查询数据是否被使用：若被使用，不能删除
//            // 若未被使用：走删除逻辑
//            map = new HashMap();
//            map.put("proTypeCode", proTypeCode);
//            List<Map> relationByProcode = modelDataDao.getRelationByProTypecode(map);
//            if (relationByProcode != null && relationByProcode.size() > 0) {
//                for (Map m : relationByProcode) {
//                    String kpId = m.get("kp_id").toString();
//                    // 该知识点在当前根节点下
//                    if (rootMap.get(kpId) != null) {
//                        KnowledgePoints knowledgePointsById = manageBackendFeign.getKnowledgePointsById(Long.valueOf(kpId));
//                        rMap.put("kpCode", knowledgePointsById.getCode());
//                        rMap.put("kpName", knowledgePointsById.getPointName());
//                        Long modelKpId = Long.valueOf(m.get("model_kp_id").toString());
//                        ModelKpVo modelKpVoById = modelKpDao.getModelKpVoById(modelKpId);
//                        rMap.put("modelId", modelKpVoById.getId());
//                        rMap.put("modelName", modelKpVoById.getName());
//                        rList.add(rMap);
//                    }
//                }
//            }
//            if (rList.size() != 0) {
//                return ApiResultHandler.buildApiResult(201, "操作成功", rList);
//            }
//            // 未被使用，走删除逻辑
//            map = new HashMap();
//            map.put("proTypeCode", proTypeCode);
//            dataService.delProNameAndKpCode(map);
//            return ApiResultHandler.buildApiResult(200, "操作成功", null);
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error(e.toString());
//            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
//        }
//    }
}
