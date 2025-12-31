package com.cloud.model.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.feign.knowledge.KnowledgeFeign;
import com.cloud.model.bean.vo.ModelDataVO;
import com.cloud.model.dao.ModelDataDao;
import com.cloud.model.dao.RelationKnowledgeDataDao;
import com.cloud.model.model.ModelData;
import com.cloud.model.service.KnowledgeProService;
import com.cloud.model.service.ModelDataService;
import com.cloud.utils.CollectionsCustomer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.attribute.FileOwnerAttributeView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/modelData")
@ApiModel(value = "知识模板数据对接管理")
@Slf4j
public class ModelDataController {

    @Autowired
    private ModelDataService modelDataService;
    /**
     * 知识点 属性查询Service
     */
    @Autowired
    private KnowledgeProService knowledgeProService;

    @Autowired
    ModelDataDao modelDataDao;


    @ApiOperation("模板组件对接数据(废弃)")
    @PostMapping("/add")
    public ApiResult add(@RequestBody ModelData modelData) {
        log.info("ModelDataController#add-->modelData={}", JSON.toJSONString(modelData));
        if (modelDataService.add(modelData)) {
            return ApiResultHandler.buildApiResult(200, "添加成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "添加失败", null);
    }

    @Autowired
    KnowledgeFeign knowledgeFeign;

    /**
     * @author:胡立涛
     * @description: TODO 查询模板下所有的组件信息
     * @date: 2024/7/23
     * @param: [modelData]
     * @return: com.cloud.core.ApiResult
     */
    @ApiOperation("点击具体模板，查询该模板下所有的组件信息 (模板和知识点关联Id)modelKpId")
    @PostMapping("/getAssemblyListByModelKpId")
    public ApiResult getAssemblyListByTemplate(@RequestBody ModelData modelData) {
        log.info("ModelDataController#getAssemblyListByTemplate-->params: modelData={}", JSON.toJSONString(modelData));
        Map<String, List<ModelDataVO>> assemblyProListMap = new HashMap<>();
        // 知识关系数据挂接
        if (!StringUtils.isEmpty(modelData.getFlg()) && modelData.getFlg().equals("1")) {
            Map<String, Object> map = new HashMap();
            map.put("modelKpId", modelData.getModelKpId());
            List<Map> relationKnowledgeAssemblyList = modelDataDao.getRelationKnowledgeAssemblyList(map);
            return ApiResultHandler.buildApiResult(200, "查询模板挂接组件信息", relationKnowledgeAssemblyList);
        }
        try {
            List<ModelDataVO> modelDataVOList = this.modelDataService.getAssemblyListByTemplate(modelData);
            List<ModelDataVO> newList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(modelDataVOList)) {
                for (ModelDataVO bean : modelDataVOList) {
                    ModelData modelData1 = modelDataDao.selectById(bean.getId());
                    String proId = modelData1.getProId();
                    String proTypeId = modelData1.getProTypeName();
                    String assemblyName = modelData1.getAssemblyName();
                    // 基础模版中的图片查询｜多格式文件模板（listKnowModal)|
                    if (assemblyName.equals("infoModal") || assemblyName.equals("listKnowModal")
                            || assemblyName.equals("knowledge") || assemblyName.equals("resources")) {
                        Map knowledgePointsById = knowledgeFeign.getKnowledgePointsById(proId);
                        String proName = knowledgePointsById == null ? "" : knowledgePointsById.get("name").toString();
                        // 查询分组信息
                        Map relationGroupById = knowledgeFeign.getRelationGroupById(proTypeId);
                        String proTypeName = relationGroupById == null ? "" : relationGroupById.get("name").toString();
                        bean.setProId(proId);
                        bean.setProProname(proName);
                        bean.setProCode(proId);
                        bean.setProTypeCode(proTypeId);
                        bean.setProTypeName(proTypeName);
                    } else {
                        Map parMap = new HashMap();
                        parMap.put("id", proId);
                        parMap.put("kpId", bean.getKpId());
                        Map proDetail = knowledgeFeign.getProDetail(parMap);
                        if (!proDetail.isEmpty() && proDetail != null) {
                            bean.setProId(proDetail.get("id").toString());
                            bean.setProProname(proDetail.get("name").toString());
                            bean.setProCode(proDetail.get("id").toString());
                            parMap = new HashMap();
                            parMap.put("id", proTypeId);
                            parMap.put("kpId", bean.getKpId());
                            proDetail = knowledgeFeign.getProDetail(parMap);
                            bean.setProTypeCode(proDetail.get("id").toString());
                            bean.setProTypeName(proDetail.get("name").toString());
                        }
                    }
                    newList.add(bean);
                }
                assemblyProListMap = newList.stream().collect(Collectors.groupingBy(ModelDataVO::getAssemblyName));
            }
            log.info("ModelDataController#getAssemblyListByTemplate-->result: Map<String, List<ModelDataVO>> :{} ", JSON.toJSONString(assemblyProListMap));
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "查询模板挂接组件信息异常", e.toString());
        }
        return ApiResultHandler.buildApiResult(200, "查询模板挂接组件信息", assemblyProListMap);
    }


    /**
     * @param modelData
     * @return
     */
    @ApiOperation("[挂接] 模板组件知识点属性")
    @PostMapping("/addKpData")
    public ApiResult addKpData(@RequestBody ModelData modelData) {
        try {
            if (modelData.getAssemblyName() == null) {
                return ApiResultHandler.buildApiResult(100, "参数assemblyType为空", null);
            }
            // 复杂知识模板
            if (modelData.getAssemblyName().contains("complexModal")) {
                if (StringUtils.isEmpty(modelData.getSqlStr())) {
                    return ApiResultHandler.buildApiResult(100, "参数子项id（sqlStr）为空", null);
                }
            }
            // 对接知识关系数据时，只能添加一个数据关系
            if (!StringUtils.isEmpty(modelData.getFlg()) && modelData.getFlg().equals("1")) {
                QueryWrapper queryWrapper = new QueryWrapper();
                queryWrapper.eq("model_kp_id", modelData.getModelKpId());
                queryWrapper.eq("assembly_name", modelData.getAssemblyName());
                // 复杂知识模板 多个子项均为知识集时处理逻辑
                if (modelData.getSqlStr() != null) {
                    queryWrapper.eq("sql_str", modelData.getSqlStr());
                }
                List list = modelDataDao.selectList(queryWrapper);
                if (!list.isEmpty() || list.size() > 0) {
                    return ApiResultHandler.buildApiResult(101, "该模板只能绑定一个知识关系数据", null);
                }
            }
            if (modelData.getAssemblyType() == null) {
                return ApiResultHandler.buildApiResult(200, "模板对接数据成功", JSON.toJSONString(modelData));
            }
            this.modelDataService.saveOrUpdate(modelData);
        } catch (DuplicateKeyException e1) {
            return ApiResultHandler.buildApiResult(200, "该属性已绑定", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "模板对接数据异常", e.getMessage());
        }
        return ApiResultHandler.buildApiResult(200, "模板对接数据成功", JSON.toJSONString(modelData));
    }

}
