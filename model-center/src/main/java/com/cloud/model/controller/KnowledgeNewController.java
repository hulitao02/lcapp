package com.cloud.model.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.config.filter.IFilterConfig;
import com.aliyun.oss.common.utils.LogUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.core.ResultMesEnum;
import com.cloud.feign.exam.ExamFeign;
import com.cloud.feign.knowledge.KnowledgeFeign;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.bean.vo.KnowledgeProVO;
import com.cloud.model.bean.vo.ModelDataVO;
import com.cloud.model.bean.vo.PlanVO;
import com.cloud.model.bean.vo.RelationBean;
import com.cloud.model.common.KnowledgePoints;
import com.cloud.model.common.Page;
import com.cloud.model.dao.*;
import com.cloud.model.model.*;
import com.cloud.model.service.*;
import com.cloud.model.user.AppUser;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.utils.CollectionsCustomer;
import com.cloud.utils.DateConvertUtils;
import com.cloud.utils.StringUtils;
import com.google.gson.Gson;
import com.hp.hpl.sparta.Document;
import com.sun.media.sound.FFT;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import tool.PooledHttpClientAdaptor;

import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/knowledge")
@ApiModel(value = "知识controller")
@Slf4j
@RefreshScope
public class KnowledgeNewController {

    // 文件服务地址
    @Value(value = "${file_server}")
    private String fileServer;
    // 图谱图片访问地址
    @Value(value = "${tupu_pic_server}")
    private String tupuPicServer;
    // 知识图谱访问地址
    @Value(value = "${tupu_server}")
    private String tupuServer;
    // 收藏知识
    @Autowired
    private CollectKnowledgeService collectKnowledgeService;
    @Autowired
    CollectKnowledgeDao collectKnowledgeDao;
    @Autowired
    ModelKpDao modelKpDao;
    @Autowired
    KnowledgeProService knowledgeProService;
    // 挂接知识service
    @Autowired
    ModelDataService modelDataService;
    // 知识点和模板Service
    @Autowired
    ModelKpService modelKpService;
    // 关注servcie
    @Autowired
    FocusKpService focusKpService;
    @Autowired
    ManageBackendFeign manageBackendFeign;
    @Autowired
    KnowledgeViewDao knowledgeViewDao;
    @Autowired
    FocusKpDao focusKpDao;
    @Autowired
    ModelDataDao modelDataDao;
    @Autowired
    KnowledgeViewService knowledgeViewService;
    @Autowired
    KnowledgePicDao knowledgePicDao;
    @Autowired
    private StudyPlanService studyPlanService;
    @Autowired
    private StudyKnowledgeService studyKnowledgeService;
    @Autowired
    KnowledgeRelationDao knowledgeRelationDao;

    @Autowired
    ExamFeign examFeign;
    @Autowired
    SysDepartmentFeign sysDepartmentFeign;
    @Autowired
    AirPortDao airPortDao;
    @Autowired
    RelationKnowledgeDataDao relationKnowledgeDataDao;
    @Autowired
    ModelControlService modelControlService;
    @Autowired
    KnowledgeTifDao knowledgeTifDao;

    /**
     * @author:胡立涛
     * @description: TODO 根据知识点id查询知识点分组及分组下的属性信息
     * @date: 2024/7/17
     * @param: [knowledgePointId, proTypeName, status, type, flg]
     * @return: com.cloud.core.ApiResult
     */
    @ApiOperation("通过知识点，查询知识点的属性")
    @GetMapping("/getProListByPointId")
    public ApiResult getProListByPointId(@RequestParam(value = "knowledgePointId") String knowledgePointId,
                                         @RequestParam(value = "proTypeId", required = false) String proTypeName,
                                         @RequestParam(value = "status") Integer status,
                                         @RequestParam(value = "type", required = false) Integer type,
                                         @RequestParam(value = "flg", required = false) Integer flg
    ) {
        try {
            if (type != null && type == 1) {
                List<Map> rList = knowledgeFeign.getProListByPointId(knowledgePointId);
                return ApiResultHandler.buildApiResult(200, "操作成功", rList);
            }
            List<Map> groupList = new ArrayList<>();
            List<Map> relationGroup = knowledgeFeign.getRelationGroup(knowledgePointId);
            for (Map map : relationGroup) {
                Map group = new HashMap();
                String relationId = map.get("id").toString();
                String relationName = map.get("name").toString();
                group.put("isProperty", 2);
                group.put("proTypeName", relationName);
                // 查询分组下的属性信息
                Map m = new HashMap();
                m.put("kpId", knowledgePointId);
                m.put("relationId", relationId);
                List<Map> relationPro = knowledgeFeign.getRelationPro(m);
                List<Map> relationProList = new ArrayList<>();
                if (relationPro != null && relationPro.size() > 0) {
                    for (Map pro : relationPro) {
                        Map proMap = new HashMap();
                        proMap.put("id", pro.get("id"));
                        proMap.put("isProperty", 0);
                        proMap.put("kpId", knowledgePointId);
                        proMap.put("kpIdString", knowledgePointId);
                        proMap.put("proCode", pro.get("id"));
                        proMap.put("proProname", pro.get("name"));
                        proMap.put("proTypeCode", relationId);
                        proMap.put("proTypeName", relationName);
                        proMap.put("childrenList", null);
                        relationProList.add(proMap);
                    }
                }
                group.put("childrenList", relationProList);
                groupList.add(group);
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", groupList);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 知识推荐
     * @date: 2024/7/18
     * @param: [pageNo, pageSize, userId, kpIds, sensesName, kps]
     * @return: com.cloud.core.ApiResult
     */
    @ApiOperation("推荐知识分页列表")
    @GetMapping("/listKnowledgePage")
    public ApiResult listKnowledgePage(@RequestParam(value = "pageNo") int pageNo,
                                       @RequestParam(value = "pageSize") int pageSize,
                                       @RequestParam(value = "userId") int userId,
                                       @RequestParam(value = "kpIds", required = false) String kpIds,
                                       @RequestParam(value = "online", required = false) String online,
                                       @RequestParam(value = "sensesName", required = false) String sensesName,
                                       @RequestParam(value = "kps", required = false) String kps) {
        try {
            List<String> kpList = new ArrayList<>();
            Map rMap = new HashMap();
            if (kpIds != null && kpIds.trim().length() > 0) {
                String[] kpIdArr = kpIds.split(",");
                for (int i = 0; i < kpIdArr.length; i++) {
                    String kpId = kpIdArr[i];
                    QueryWrapper queryWrapper = new QueryWrapper();
                    queryWrapper.eq("status", 2);
                    queryWrapper.eq("kp_id", kpId);
                    Integer count = modelKpDao.selectCount(queryWrapper);
                    if (count > 0) {
                        kpList.add(kpId);
                    }
                }
                if (kpList.size() == 0) {
                    Map map = new HashMap();
                    map.put("result", rMap);
                    return ApiResultHandler.buildApiResult(ResultMesEnum.SUCCESS.getResultCode(), "知识推荐列表返回成功", map);
                }
                // 个人能力评估：能力分布的知识点数据获取
                List<Map> abilityzz = examFeign.abilityzz(new HashMap());
                // 将知识点保存到xx_class表中
                Map knowPar = new HashMap();
                knowPar.put("userId", userId);
                knowPar.put("list", kpList);
                knowPar.put("abilityzz", abilityzz);
                knowledgeFeign.saveClass(knowPar);

                Map parMap = new HashMap();
                int index = (pageNo - 1) * pageSize;
                parMap.put("pageNo", index);
                parMap.put("pageSize", pageSize);
                parMap.put("kpIds", kpList);
                if (sensesName != null && sensesName != "") {
                    sensesName = "%" + sensesName + "%";
                }
                parMap.put("sensesName", sensesName);
                List<Map> list = knowledgeFeign.listKnowledgePage(parMap);
                rMap.put("current", pageNo);
                rMap.put("size", pageSize);
                int total = knowledgeFeign.listKnowledgePageCount(parMap);
                rMap.put("total", total);
                Page<Map> mapPage = new Page<>(total, list);
                List<Map> records = new ArrayList<>();
                if (list != null && list.size() > 0) {
                    for (Map map : list) {
                        Map detal = new HashMap();
                        detal.put("bkClassLabel", map.get("point_name"));
                        detal.put("classId", map.get("class_id"));
                        detal.put("kpId", map.get("class_id"));
                        detal.put("sensesId", map.get("id"));
                        detal.put("sensesName", map.get("name"));
                        // 查看知识是否收藏
                        QueryWrapper queryWrapper = new QueryWrapper();
                        queryWrapper.eq("senses_id", map.get("id"));
                        queryWrapper.eq("user_id", userId);
                        Integer integer = collectKnowledgeDao.selectCount(queryWrapper);
                        detal.put("collectStatus", integer == 0 ? 0 : 1);
                        // 封面图
                        QueryWrapper query = new QueryWrapper();
                        query.eq("kp_id", map.get("class_id"));
                        KnowledgePic knowledgePic = knowledgePicDao.selectOne(query);
                        String photo = "";
                        if (knowledgePic != null) {
                            Map picMap = new HashMap();
                            picMap.put("knowledgeId", map.get("id"));
                            picMap.put("proId", knowledgePic.getProId());
                            List<Map> proValue = knowledgeFeign.getProValue(picMap);
                            if (proValue != null && proValue.size() > 0) {
                                photo = tupuPicServer + proValue.get(0).get("data_property_value");
                            }
                        }
                        detal.put("photo", photo);
                        detal.put("studyStatus", getViewState(userId, map.get("id").toString()));
                        records.add(detal);
                    }
                }
                rMap.put("records", records);
            }
            Map map = new HashMap();
            map.put("result", rMap);
            return ApiResultHandler.buildApiResult(ResultMesEnum.SUCCESS.getResultCode(), "知识推荐列表返回成功", map);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[推荐知识列表] 解析异常:{} ,", e.getMessage());
            return ApiResultHandler.buildApiResult(ResultMesEnum.INTERNAL_SERVER_ERROR.getResultCode(), "知识推荐列表返回失败", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据用户id，知识点id查看是否收藏过该知识 0：未收藏 1：收藏
     * @date: 2022/1/13
     * @param: [userId, sensesId]
     * @return: int
     */
    public int getCollectState(long userId, String sensesId) {
        // 根据用户id，知识id查询收藏表
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("sensesId", sensesId);
        Map<String, Object> collectKnowledge = collectKnowledgeDao.getCollectKnowledge(map);
//        collectKnowledge = CollectionsCustomer.builder().build().mapToLowerCase(collectKnowledge);
        if (collectKnowledge == null) {
            return 0;
        } else if (Integer.parseInt(collectKnowledge.get("collect_status").toString()) == 0) {
            return 0;
        }
        return 1;
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据用户id，知识点id查看是否浏览过该知识 0：未浏览 1：浏览
     * @date: 2022/1/13
     * @param: [userId, sensesId]
     * @return: int
     */
    public int getViewState(long userId, String sensesId) {
        // 根据用户id，知识id查询收藏表
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("sensesId", sensesId);
        Map<String, Object> knowledgeView = knowledgeViewDao.getKnowledgeView(map);
//        knowledgeView = CollectionsCustomer.builder().build().mapToLowerCase(knowledgeView);
        if (knowledgeView == null) {
            return 0;
        }
        return 1;
    }


    /**
     * @author:胡立涛
     * @description: TODO 收藏列表分页查询
     * @date: 2022/1/13
     * @param: [userId, pageNo, pageSize, collectStatus, keyWord]
     * @return: com.cloud.core.ApiResult
     */
    @ApiOperation("收藏列表分页查询")
    @GetMapping("/pageUserCollectKnowledge")
    public ApiResult pageUserCollectKnowledge(@RequestParam("userId") int userId,
                                              @RequestParam("pageNo") int pageNo,
                                              @RequestParam("pageSize") int pageSize,
                                              @RequestParam(value = "collectStatus", required = false) Integer collectStatus,
                                              @RequestParam(value = "keyWord", required = false) String keyWord) {
        try {
            CollectKnowledgeBean paramsCb = new CollectKnowledgeBean();
            paramsCb.setUserId(userId);
            if (Objects.nonNull(collectStatus)) {
                paramsCb.setCollectStatus(collectStatus);
            }
            if (Objects.nonNull(keyWord)) {
                paramsCb.setSensesName(keyWord);
            }
            Set<String> kpIdsbyUserId = sysDepartmentFeign.getKpIdsbyUserId(AppUserUtil.getLoginAppUser().getId());
            IPage<CollectKnowledgeBean>
                    pageCollection = this.collectKnowledgeService.pageUserCollectKnowledge(pageNo, pageSize, paramsCb);
            if (pageCollection != null) {
                List<CollectKnowledgeBean> records = pageCollection.getRecords();
                records.sort(Comparator.comparing(
                        (CollectKnowledgeBean v) -> {
                            return v.getCollectDate();
                        }
                ).reversed());
                pageCollection.setRecords(records);
                for (CollectKnowledgeBean bean : records) {
                    // 查看是否浏览过
                    int viewState = getViewState(bean.getUserId(), bean.getSensesId());
                    bean.setStudyStatus(viewState);
                    //根据用户拥有的知识权限判断是否可以查看知识
                    bean.setKpId(bean.getClassId());
                    if (kpIdsbyUserId.contains(bean.getClassId())) {
                        bean.setFlag(true);
                    } else {
                        bean.setFlag(false);
                    }
                    // 根据kpId查询封面图信息
                    QueryWrapper query = new QueryWrapper();
                    query.eq("kp_id", bean.getClassId());
                    KnowledgePic knowledgePic = knowledgePicDao.selectOne(query);
                    String photo = "";
                    if (knowledgePic != null) {
                        Map map = new HashMap();
                        map.put("proId", knowledgePic.getProId());
                        map.put("knowledgeId", bean.getSensesId());
                        List<Map> proValue = knowledgeFeign.getProValue(map);
                        if (proValue != null && proValue.size() > 0) {
                            photo = tupuPicServer + proValue.get(0).get("data_property_value").toString();
                        }
                    }
                    // 封面图
                    bean.setPhoto(photo);
                }
            }
            return ApiResultHandler.buildApiResult(200, "查询收藏知识成功", pageCollection);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }

    }


    /**
     * @author:胡立涛
     * @description: TODO 浏览记录分页查询
     * @date: 2022/1/13
     * @param: [userId, pageNo, pageSize, collectStatus, keyWord]
     * @return: com.cloud.core.ApiResult
     */
    @ApiOperation("浏览记录分页查询")
    @GetMapping("/pageUserKnowledgeView")
    public ApiResult pageUserKnowledgeView(@RequestParam("userId") int userId,
                                           @RequestParam("pageNo") int pageNo,
                                           @RequestParam("pageSize") int pageSize,
                                           @RequestParam(value = "keyWord", required = false) String keyWord) {
        try {
            KnowledgeViewBean paramsCb = new KnowledgeViewBean();
            paramsCb.setUserId(userId);
            if (Objects.nonNull(keyWord)) {
                paramsCb.setSensesName(keyWord);
            }
            IPage<KnowledgeViewBean> pageCollection = knowledgeViewService.pageUserKnowledgeView(pageNo, pageSize, paramsCb);
            return ApiResultHandler.buildApiResult(200, "操作成功", pageCollection);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 用户收藏、取消收藏知识
     * @date: 2022/1/13
     * @param: [paramsCollection]
     * @return: com.cloud.core.ApiResult
     */
    @ApiOperation("用户收藏、取消收藏知识")
    @PostMapping("/saveUserCollections")
    public ApiResult saveUserCollections(@RequestBody CollectKnowledgeBean paramsCollection) {
        try {
            // 根据用户id，知识点id查询用户是否收藏过该信息
            Map<String, Object> map = new HashMap<>();
            map.put("userId", paramsCollection.getUserId());
            map.put("sensesId", paramsCollection.getSensesId());
            Map<String, Object> collectKnowledge = collectKnowledgeDao.getCollectKnowledge(map);
//            collectKnowledge = CollectionsCustomer.builder().build().mapToLowerCase(collectKnowledge);

            paramsCollection.setCollectDate(new Timestamp(System.currentTimeMillis()));
            if (collectKnowledge != null) {
                paramsCollection.setId(Integer.parseInt(collectKnowledge.get("id").toString()));
                paramsCollection.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            } else {
                paramsCollection.setCreateTime(new Timestamp(System.currentTimeMillis()));

            }
            this.collectKnowledgeService.saveOrUpdate(paramsCollection);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 查看用户是否关注该知识点 0:未关注 1：已关注
     * @date: 2022/1/14
     * @param: [userId, kpId]
     * @return: int
     */
    public int getFocuState(int userId, String kpId) {
        QueryWrapper queryw = new QueryWrapper<FocusKp>();
        queryw.eq("user_id", userId);
        queryw.eq("kp_id", kpId);
        Map map = focusKpService.getMap(queryw);
        if (map == null) {
            return 0;
        }
        return 1;
    }


    /**
     * @author:胡立涛
     * @description: TODO 知识预览
     * @date: 2022/1/7
     * @param: [modelKpId, modelId, knowledgeId, userId] type:标识是否为复杂模板 1:是 其他：否
     * @return: com.cloud.core.ApiResult
     */
    @ApiOperation("知识预览接口")
    @GetMapping("/showKnowledgeByModelKpId")
    public ApiResult getKnowledgeByTemplate(@RequestParam(value = "modelKpId") String modelKpId,
                                            @RequestParam(value = "modelId", required = false) Long modelId,
                                            @RequestParam(value = "knowledgeId") String knowledgeId,
                                            @RequestParam(value = "userId", required = false) Integer userId,
                                            @RequestParam(value = "type", required = false) Integer type) {
        try {
            Map rMap = new HashMap();
            Map<String, Integer> typeMap = new HashMap<>();
            typeMap.put("textModal", 1);
            typeMap.put("tableModal", 1);
            typeMap.put("listModal", 0);
            typeMap.put("webGisModal", 0);
            typeMap.put("fileModal", 0);
            typeMap.put("videoModal", 0);
            typeMap.put("infoModal", 0);
            typeMap.put("listKnowModal", 0);
            // 引入模板
            typeMap.put("embedHtmlModal", 1);
            // 跳转模板
            typeMap.put("jumpHtmlModal", 1);
            ModelKp modelKp = modelKpService.getById(Integer.parseInt(modelKpId));
            // 根据知识点id查询知识点名称
            Map point = knowledgeFeign.getKnowledgePointsById(modelKp.getKpId());
            if (point == null) {
                return ApiResultHandler.buildApiResult(100, "不存在该知识点：" + modelKp.getKpId(), null);
            }
            // 知识点信息 收藏知识 关注知识点
            Map<String, Object> voKp = new HashMap<>();
            voKp.put("kpId", modelKp.getKpId());
            voKp.put("kpName", point.get("name").toString());
            voKp.put("collectStatus", getCollectState(userId, knowledgeId));
            voKp.put("constraintForce", getFocuState(userId, modelKp.getKpId()));
            voKp.put("classId", modelKp.getKpId());
            rMap.put("kpVO", voKp);
            // 查询模板类型
            Map<String, Object> pMap = new HashMap<>();
            pMap.put("modelKpId", Integer.parseInt(modelKpId));
            // 复杂模板
            if (type != null && type == 1) {
                pMap.put("constraintForce", "1");
            }
            List<Map> nameList = modelDataDao.getAssemblyName(pMap);
            if (nameList != null && nameList.size() > 0) {
                for (Map map : nameList) {
                    List<Map> modelList = new ArrayList<>();
                    String name = map.get("assembly_name").toString();
                    // 根据assembly_name，modelKpId查询信息
                    pMap.put("assemblyName", name);
                    List<Map> proInfoList = modelDataDao.getProInfoList(pMap);
                    // 属性
                    if ((type != null && type == 1) || (typeMap.get(name) != null && typeMap.get(name) == 1)) {
                        for (Map proMap : proInfoList) {
                            Map<String, Object> otherProValue = new HashMap<>();
                            // 根据属性code，知识id查询属性值
                            String proCode = proMap.get("pro_code").toString();
                            Map parMap = new HashMap();
                            parMap.put("knowledgeId", knowledgeId);
                            parMap.put("proId", proCode);
                            List<Map> list = knowledgeFeign.getProValue(parMap);
                            String rValue = "";
                            if (list != null && list.size() > 0) {
                                for (Map bean : list) {
                                    String proValue = bean.get("data_property_value").toString();
                                    if (proMap.get("assembly_type").equals("imgUrl")) {
                                        proValue = tupuPicServer + bean.get("data_property_value").toString();
                                    }
                                    otherProValue.put("proProvalue", proValue);
                                    otherProValue.put("assemblyType", proMap.get("assembly_type"));
                                    otherProValue.put("assemblyName", proMap.get("assembly_name"));
                                    otherProValue.put("proId", proMap.get("pro_code"));
                                    // 根据知识点，属性id查询属性信息
                                    Map m = new HashMap();
                                    m.put("kpId", modelKp.getKpId());
                                    m.put("id", proCode);
                                    Map proDetail = knowledgeFeign.getProDetail(m);
                                    otherProValue.put("proProname", proDetail.get("name"));
                                    otherProValue.put("sqlStr", proMap.get("sql_str"));
                                    modelList.add(otherProValue);
                                }
                            }
                        }
                        rMap.put(name, modelList);
                    } else {
                        if (name.equals("listKnowModal")) {
                            List list = new ArrayList();
                            for (Map relMap : proInfoList) {
                                String proCode = relMap.get("pro_code").toString();
                                String proTypeCode = relMap.get("pro_type_code").toString();
                                Map knowledgePointsById = knowledgeFeign.getKnowledgePointsById(proCode);
                                Map m = new HashMap();
                                m.put("proProname", knowledgePointsById.get("name"));
                                // 查询文档概念下的知识数据
                                List<Map> stepOneList = knowledgeFeign.getStepOne(proCode);
                                List<String> targetIds = null;
                                if (stepOneList != null && stepOneList.size() > 0) {
                                    targetIds = new ArrayList<>();
                                    for (Map stepOneMap : stepOneList) {
                                        targetIds.add(stepOneMap.get("id").toString());
                                    }
                                }
                                // 查询概念下实际关联的知识
                                Map parMap = new HashMap();
                                parMap.put("proTypeCode", proTypeCode);
                                parMap.put("knowledgeId", knowledgeId);
                                parMap.put("targetIds", targetIds);
                                List<Map> stepTwoList = knowledgeFeign.getStepTwo(parMap);
                                List resultList = new ArrayList();
                                if (stepTwoList != null && stepTwoList.size() > 0) {
                                    for (Map stepTwoMap : stepTwoList) {
                                        // 查询知识对应的属性值
                                        String targetKnowledgeId = stepTwoMap.get("range_individual_id").toString();
                                        List<Map> stepThreeList = knowledgeFeign.getStepThree(targetKnowledgeId);
                                        if (stepThreeList != null && stepThreeList.size() > 0) {
                                            String fileUrls = "";
                                            Map resultMap = resultMap = new HashMap();
                                            for (Map stepThteeMap : stepThreeList) {
                                                String value = stepThteeMap.get("data_property_value") == null ? "" : stepThteeMap.get("data_property_value").toString();
                                                String label = stepThteeMap.get("label") == null ? "" : stepThteeMap.get("label").toString();
                                                if (label != "" && label.equals("fileUrl")) {
                                                    fileUrls += tupuPicServer + value + ";";
                                                } else {
                                                    resultMap.put(label, value);
                                                }
                                            }
                                            if (fileUrls != "") {
                                                resultMap.put("fileUrl", fileUrls.substring(0, fileUrls.length() - 1));
                                            }
                                            resultList.add(resultMap);
                                        }
                                    }
                                }
                                m.put("list", resultList);
                                list.add(m);
                                rMap.put(name, list);
                            }
                        } else {
                            // 关系
                            Map map_1 = proInfoList.get(0);
                            // 根据 知识点id，知识id，关系id，关联概念id，查询关系数据
                            Map m = new HashMap();
                            m.put("proId", map_1.get("pro_code"));
                            m.put("proTypeCode", map_1.get("pro_type_code"));
                            m.put("knowledgeId", knowledgeId);
                            List<Map> relationProVal = knowledgeFeign.getRelationProVal(m);
                            List<Map> imageList = new ArrayList<>();
                            String imageAddress = "";
                            String imageName = "";
                            for (Map detail : relationProVal) {
                                System.out.println("-----图片信息：" + JSON.toJSON(detail));
                                String data_type = detail.get("data_type") == null ? "" : detail.get("data_type").toString();
                                if (data_type != "" && data_type.equals("IMAGE")) {
                                    imageAddress += tupuPicServer + detail.get("data_property_value").toString() + ";";
                                } else {
                                    imageName = detail.get("data_property_value").toString();
                                }
                            }
                            Map imageMap = new HashMap();
                            imageMap.put("imageName", imageName);
                            imageMap.put("imageUrl", imageAddress == "" ? "" : imageAddress.substring(0, imageAddress.length() - 1));
                            imageList.add(imageMap);
                            Map m1 = new HashMap();
                            m1.put("list", imageList);
                            m1.put("proProname", "tu");
                            rMap.put(name, m1);
                        }
                    }
                }
            }
            return ApiResultHandler.buildApiResult(200, "知识预览返回成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常：" + e.toString(), null);
        }
    }


    /**
     * 知识预览时，相关模板预览
     *
     * @param kpointId
     * @return
     */
    @ApiOperation("知识预览时，相关模板预览")
    @GetMapping("/showKnowledgeRelatedModelList")
    public ApiResult showKnowledgeRelatedModelList(@RequestParam(value = "kpointId") String kpointId) {
        log.info("KnowledgeController#showKnowledgeRelatedModelList--> params : kpointId={}", kpointId);
        ModelData params = new ModelData();
        params.setKpId(kpointId);
        List<ModelDataVO> modelKpIdList = this.modelDataService.getModelKpIdListBykpId(params);
        if (CollectionUtils.isNotEmpty(modelKpIdList)) {
            modelKpIdList = modelKpIdList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(
                    () -> new TreeSet<>(Comparator.comparing(ModelDataVO::getModelKpId))), ArrayList::new
            ));
            return ApiResultHandler.buildApiResult(200, "知识根据模板展示", modelKpIdList);
        }
        return ApiResultHandler.buildApiResult(500, "该知识点，还没有挂接数据", modelKpIdList);
    }


    /**
     * @author:胡立涛
     * @description: TODO 调用第三方服务 根据知识id，属性code查询属性值
     * @date: 2022/1/14
     * @param: [url] 接口url
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     */
    public Map<String, Object> getOtherProValue(String url) {
        String str = getOther(url);
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotEmpty(str)) {
            JSONObject jsonObject = JSON.parseObject(str);
            String code = jsonObject.getString("code");
            if (code.equals("200")) {
                map = (Map) jsonObject.get("result");
            }
        }
        return map;
    }


    /**
     * @author:胡立涛
     * @description: TODO 调用第三方服务 根据知识点code，知识点id，关系id，关联概念id查询关系数据
     * @date: 2022/1/14
     * @param: [url] 接口url
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     */
    public Map<String, Object> getOtherRelation(String url) {
        String str = getOther(url);
        // 模拟数据获取
        // String str = mnProRelation();
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotEmpty(str)) {
            JSONObject jsonObject = JSON.parseObject(str);
            String code = jsonObject.getString("code");
            if (code.equals("200")) {
                map = (Map) jsonObject.get("result");
            }
        }
        return map;
    }


    /**
     * @author:胡立涛
     * @description: TODO 调用第三方接口
     * @date: 2022/1/14
     * @param: [url]
     * @return: java.lang.String
     */
    public String getOther(String url) {
        PooledHttpClientAdaptor adaptor = new PooledHttpClientAdaptor();
        Map<String, String> headMap = new HashMap<>();
        //知识点下的所有的知识
        return adaptor.doGet(url, headMap, Collections.emptyMap());
    }


    /**
     * @author:胡立涛
     * @description: TODO 查询知识点下的所有知识
     * @date: 2024/7/17
     * @param: [knowledgePointId]
     * @return: com.cloud.core.ApiResult
     */
    @ApiOperation("得到知识点下，所有的知识")
    @GetMapping("/getAllKnowledgeListByPointId")
    public ApiResult getAllKnowledgeListByPointId(@RequestParam(value = "knowledgePointId", required = true) String knowledgePointId) {
        try {
            List<Map> list = knowledgeFeign.getAllKnowledgeListByPointId(knowledgePointId);
            Map bean = new HashMap();
            if (list != null && list.size() > 0) {
                for (Map map : list) {
                    bean.put(map.get("id"), map.get("name").toString());
                }
            }
            return ApiResultHandler.buildApiResult(ResultMesEnum.SUCCESS.getResultCode(), "查询知识点下所有的知识", bean);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(ResultMesEnum.SUCCESS.getResultCode(), "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 知识挂接的模板列表查询
     * @date: 2022/1/13
     * @param: [knowledgeId, kpId, userId, sensesName, bkClassName]
     * @return: com.cloud.core.ApiResult
     */
    @ApiOperation("知识挂接的模板列表查询")
    @GetMapping("/showKnowledgeByKpId")
    public ApiResult showKnowledgeByKpId(@RequestParam(value = "knowledgeId", required = false) String knowledgeId,
                                         @RequestParam(value = "kpId") String kpId,
                                         @RequestParam(value = "userId", required = false) Integer userId,
                                         @RequestParam(value = "sensesName", required = false) String sensesName,
                                         @RequestParam(value = "bkClassName", required = false) String bkClassName,
                                         @RequestParam(value = "flg", required = false) Integer flg
    ) {
        try {
            Long[] userIds = new Long[1];
            userIds[0] = Long.valueOf(userId);
            List<Map<String, Object>> userKpIds = sysDepartmentFeign.getUserKpIds(userIds);
            if (userKpIds.isEmpty()) {
                return ApiResultHandler.buildApiResult(300, "抱歉，您没有查看该数据的权限", null);
            }
            Map<String, Integer> kpMap = new HashMap<>();
            for (Map map : userKpIds) {
                kpMap.put(map.get("kp_id").toString(), 1);
            }
            if (kpMap.get(kpId) == null) {
                return ApiResultHandler.buildApiResult(300, "抱歉，您没有查看该数据的权限", null);
            }
            ModelData params = new ModelData();
            params.setKpId(kpId);
            params.setStatus(2);
            List<ModelDataVO> modelKpIdList = this.modelDataService.getModelKpIdListBykpId(params);

            if (CollectionUtils.isNotEmpty(modelKpIdList)) {
                for (ModelDataVO bean : modelKpIdList) {
                    if (!StringUtils.isEmpty(bean.getPicPath())) {
                        bean.setPicPath(fileServer + bean.getPicPath());
                    }
                }
                KnowledgeViewBean knowledgeView = null;
                if (Objects.nonNull(flg) && flg.intValue() == 1) {
                    // 浏览记录
                    QueryWrapper queryWrapper = new QueryWrapper();
                    queryWrapper.eq("user_id", userId);
                    queryWrapper.eq("senses_id", knowledgeId);
                    knowledgeView = knowledgeViewService.getOne(queryWrapper);
                    if (knowledgeView == null) {
                        KnowledgeViewBean bean = new KnowledgeViewBean();
                        bean.setUserId(userId);
                        bean.setSensesName(sensesName);
                        bean.setBkClassLabel(bkClassName);
                        bean.setSensesId(knowledgeId);
                        bean.setClassId(kpId);
                        bean.setCreateTime(new Timestamp(System.currentTimeMillis()));
                        bean.setStudyDate(new Timestamp(System.currentTimeMillis()));
                        knowledgeViewService.save(bean);
                    } else {
                        log.info(" 用户:{} ,学习知识:{} 已学习当前的知识", userId, knowledgeId);
                        knowledgeView.setStudyDate(new Timestamp(System.currentTimeMillis()));
                        knowledgeViewService.updateById(knowledgeView);
                    }
                }
                /**
                 *  保存学习的同时，看是该知识点是否于学习计划关联，并且是否在学习计划时间内，
                 *  增加 逻辑时间 4-27 ，修改8-18
                 */
                try {
                    if (knowledgeId != null) {

                    }
//                  判断上边的逻辑，是否有学习的记录
                    if (Objects.isNull(knowledgeView)) {
                        QueryWrapper queryWrapper = new QueryWrapper();
                        queryWrapper.eq("user_id", userId);
                        queryWrapper.eq("senses_id", knowledgeId);
                        knowledgeView = knowledgeViewService.getOne(queryWrapper);
                        if (Objects.isNull(knowledgeView)) {
                            knowledgeView = new KnowledgeViewBean();
                            knowledgeView.setUserId(userId);
                            knowledgeView.setSensesName(sensesName);
                            knowledgeView.setBkClassLabel(bkClassName);
                            knowledgeView.setSensesId(knowledgeId);
                            knowledgeView.setCreateTime(new Timestamp(System.currentTimeMillis()));
                            knowledgeView.setStudyDate(new Timestamp(System.currentTimeMillis()));
                            knowledgeViewService.save(knowledgeView);
                        }
                    }
                    Date createTime = knowledgeView.getCreateTime();
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("userId", userId);
                    paramMap.put("knowledgeId", knowledgeId);
                    List<PlanVO> planKnowledgeList = this.studyPlanService.getPlanKnowledgeList(paramMap);
                    if (CollectionUtils.isNotEmpty(planKnowledgeList)) {
                        List<StudyKnowledge> toUpdateList = new ArrayList<>();
                        planKnowledgeList.stream().forEach(vo -> {
                            Integer studyStatus = vo.getStudyStatus();
                            if (Objects.isNull(studyStatus) || 2 != studyStatus.intValue()) {
                                Date endTime = vo.getEndTime();
                                Date startTime = vo.getStartTime();
//                               学习计划内完成的知识，状态改变
                                StudyKnowledge studyKnowledge = new StudyKnowledge();
                                studyKnowledge.setId(vo.getId());
                                studyKnowledge.setPlanId(vo.getPlanId());
                                if (createTime.getTime() >= startTime.getTime() && createTime.getTime() <= endTime.getTime()) {
                                    log.info("在学习计划范围内， 学习时间:[{}]  , 学习计划开始时间:[{}] , 学习计划结束时间:[{}] ",
                                            DateConvertUtils.date2Str(createTime), DateConvertUtils.date2Str(startTime), DateConvertUtils.date2Str(endTime));
                                    studyKnowledge.setStudyStatus(2);
                                } else {
                                    log.info("学习时间:[{}]  , 学习计划开始时间:[{}] , 学习计划结束时间:[{}] ",
                                            DateConvertUtils.date2Str(createTime), DateConvertUtils.date2Str(startTime), DateConvertUtils.date2Str(endTime));
                                    studyKnowledge.setStudyStatus(1);
                                }
                                toUpdateList.add(studyKnowledge);
                            }
                        });
                        if (CollectionUtils.isNotEmpty(toUpdateList)) {
                            log.info(" 用户:{} ,学习知识:{} , 更新学习计划内，知识的学习状态：{} ", userId, knowledgeId, JSON.toJSONString(toUpdateList));
                            this.studyKnowledgeService.updateBatchById(toUpdateList);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("批量更新 学习计划关联的知识学系状态异常 ");
                }
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", modelKpIdList);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 完成知识挂接
     * @date: 2022/1/13
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @ApiOperation("完成知识挂接")
    @PostMapping("/finishData")
    public ApiResult finishData(@RequestBody Map<String, Object> map) {
        try {
            modelDataService.finishData(Long.valueOf(map.get("modelKpId").toString()));
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作失败。", null);
        }
        return ApiResultHandler.buildApiResult(200, "完成知识挂接成功。", null);
    }


    /**
     * @author:胡立涛
     * @description: TODO 关注、取消关注知识点
     * @date: 2022/1/13
     * @param: [focusKp]
     * @return: com.cloud.core.ApiResult
     */
    @ApiOperation("关注知识点接口")
    @PostMapping("/saveOrUpdateFocusKP")
    public ApiResult saveFocusKP(@RequestBody FocusKp focusKp) {
        try {
            // 根据用户id，知识点id查看是否关注该知识点
            Map<String, Object> map = new HashMap<>();
            map.put("userId", focusKp.getUserId());
            map.put("kpId", focusKp.getKpId());
            Map<String, Object> rMap = focusKpDao.getFocusKp(map);
//            rMap = CollectionsCustomer.builder().build().mapToLowerCase(rMap);
            if (rMap != null) {
                focusKp.setId(Integer.parseInt(rMap.get("id").toString()));
            } else {
                focusKp.setCreateTime(new Timestamp(System.currentTimeMillis()));
            }
            focusKpService.saveOrUpdate(focusKp);
            return ApiResultHandler.buildApiResult(200, "操作成功", focusKp);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @param searchParam
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation("关注知识点 分页查询")
    @GetMapping("/selectFocusForPage")
    public ApiResult selectFocusForPage(@RequestParam(value = "searchParam", required = false) String searchParam,
                                        @RequestParam("userId") Integer userId,
                                        @RequestParam("pageNo") Integer pageNo,
                                        @RequestParam("pageSize") Integer pageSize) {

        log.info("KnowledgeController#selectFocusForPage--> params=search: {},pageNo:{} ,PageSize:{}",
                searchParam, pageNo, pageSize);
        IPage<FocusKp> pageFocus = this.focusKpService.selectFocusListForPage(searchParam, userId, pageNo, pageSize);
        return ApiResultHandler.buildApiResult(200, "关注知识点，分页返回成功", pageFocus);
    }


    /**
     * @author:胡立涛
     * @description: TODO 删除模板属性
     * @date: 2021/12/3
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "delPro")
    public ApiResult delPro(@RequestBody Map<String, Object> map) {
        try {
            modelDataService.delPro(map);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("删除模板属性异常：" + e.toString());
            return ApiResultHandler.buildApiResult(500, "删除模板属性异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 设置封面图
     * @date: 2022/1/26
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "savePhoto")
    public ApiResult savePhoto(@RequestBody KnowledgePic knowledgePic) {
        try {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("kp_id", knowledgePic.getKpId());
            KnowledgePic picDetail = knowledgePicDao.selectOne(queryWrapper);
            if (picDetail != null) {
                knowledgePic.setId(picDetail.getId());
                knowledgePicDao.updateById(knowledgePic);
            } else {
                knowledgePicDao.insert(knowledgePic);
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("封面图设置异常:" + e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据知识点获取封面图信息
     * @date: 2022/1/26
     * @param: [knowledgePic]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getPhoto")
    public ApiResult getPhoto(@RequestBody KnowledgePic knowledgePic) {
        try {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("kp_id", knowledgePic.getKpId());
            KnowledgePic result = knowledgePicDao.selectOne(queryWrapper);
            return ApiResultHandler.buildApiResult(200, "操作成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取封面图异常：" + e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 同步知识图谱数据到203数据库
     * @date: 2022/2/10
     * @param: []
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "tongbuPoint")
    public ApiResult tongbuPoint() {
        try {
            List<KnowledgePoints> all = manageBackendFeign.findAll();
            for (KnowledgePoints bean : all) {
                manageBackendFeign.updateParentId(bean);
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 同步知识图谱数据到203数据库
     * @date: 2022/2/10
     * @param:
     * @return:
     */
    @PostMapping(value = "tongbuPro")
    public ApiResult tongbuPro() {
        try {
            List<KnowledgePro> list = knowledgeProService.list();
            for (KnowledgePro bean : list) {
                // 根据知识点code查询知识点id
                Map knowledgePointsMap = manageBackendFeign.getKnowledgePointsByCode(bean.getKpCode());
//                knowledgePointsMap = CollectionsCustomer.builder().build().mapToLowerCase(knowledgePointsMap);
                // 更新kpId
                KnowledgePro byId = knowledgeProService.getById(bean.getId());
                byId.setKpId(Long.valueOf(knowledgePointsMap.get("id").toString()));
                knowledgeProService.updateById(byId);
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 获取绑定数据的知识点树形菜单
     * @date: 2022/3/14
     * @param: []
     * @return: com.cloud.core.ApiResult
     */
    @GetMapping(value = "getKnowledgeSearch")
    public ApiResult getKnowledgeSearch(@RequestParam(required = false) String kps) {

        try {
            // 获取根节点下所有知识点id
            List<Map> classById = knowledgeFeign.getKnowledgePointListFeign();
            if (classById.isEmpty() || classById.size() == 0) {
                return ApiResultHandler.buildApiResult(200, "操作成功", null);
            }
            Map rootMap = new HashMap();
            for (Map classMap : classById) {
                rootMap.put(classMap.get("id").toString(), 1);
            }
            String classIds = "";
            QueryWrapper<ModelKp> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("kp_id");
            queryWrapper.eq("status", 2);
            queryWrapper.groupBy("kp_id");
            List<ModelKp> modelKps = modelKpDao.selectList(queryWrapper);
            for (ModelKp modelKp : modelKps) {
                String kpId = modelKp.getKpId();
                // 判断该知识点是否为根节点下的知识点
                if (rootMap.get(kpId) != null) {
                    classIds += modelKp.getKpId() + ",";
                }
            }
            if (StringUtils.isNotEmpty(classIds)) {
                classIds = classIds.substring(0, classIds.length() - 1);
            }
            if (classIds == "") {
                return ApiResultHandler.buildApiResult(ResultMesEnum.SUCCESS.getResultCode(), "暂无数据", null);
            }
            Map map = new HashMap();
            map.put("ids", classIds);
            List<Map> knowledgeBindList = knowledgeFeign.getKnowledgeBind(map);
            if (StringUtils.isNotBlank(kps)) {
                List<String> list = new ArrayList<>();
                String[] split = kps.split(",");
                for (String str : split) {
                    list.add(str);
                }
                List<Map> rList = knowledgeBindList.stream().filter(e -> list.contains(Long.valueOf(String.valueOf(e.get("id"))))).collect(Collectors.toList());
                JSONArray result = listToTree(JSONArray.parseArray(JSON.toJSONString(rList)), "id", "parentid", "children");
                return ApiResultHandler.buildApiResult(200, "操作成功", result);
            }
            // 用户知识点权限
            Set<String> kpIdList = sysDepartmentFeign.getKpIdsbyUserId(AppUserUtil.getLoginAppUser().getId());
            List<Map> rList = knowledgeBindList.stream().filter(e -> kpIdList.contains(e.get("id"))).collect(Collectors.toList());
            JSONArray result = listToTree(JSONArray.parseArray(JSON.toJSONString(rList)), "id", "parentid", "children");
            return ApiResultHandler.buildApiResult(200, "操作成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 将list集合转换为树形结构
     * @date: 2022/3/14
     * @param: [arr, id, parentid, child]
     * @return: com.alibaba.fastjson.JSONArray
     */
    public static JSONArray listToTree(JSONArray arr, String id, String parentid, String child) {
        JSONArray r = new JSONArray();
        JSONObject hash = new JSONObject();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject json = (JSONObject) arr.get(i);
            hash.put(json.getString(id), json);
        }
        for (int j = 0; j < arr.size(); j++) {
            JSONObject aVal = (JSONObject) arr.get(j);
            JSONObject hashVp = (JSONObject) hash.get(aVal.get(parentid).toString());
            if (hashVp != null) {
                if (hashVp.get(child) != null) {
                    JSONArray ch = (JSONArray) hashVp.get(child);
                    ch.add(aVal);
                    hashVp.put(child, ch);
                } else {
                    JSONArray ch = new JSONArray();
                    ch.add(aVal);
                    hashVp.put(child, ch);
                }
            } else {
                r.add(aVal);
            }
        }
        return r;
    }


    /**
     * @author: 胡立涛
     * @description: TODO 知识学习模块，知识智能推荐
     * @date: 2022/5/30
     * @param: []
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getStudyKnowledge")
    public ApiResult getStudyKnowledge() {
        try {
            // 查询能力评估阀值
            String paramValue = manageBackendFeign.getParamValue(CommonConstans.eval_score);
            double evalScore = Double.valueOf(paramValue);
            // 查询低于能力评估阀值的知识点列表
            List<Map<String, Object>> pointList = examFeign.getPointList(evalScore);
            if (pointList != null && pointList.size() > 0) {
                Long[] kpIds = new Long[pointList.size()];
                for (int i = 0; i < pointList.size(); i++) {
                    kpIds[i] = Long.valueOf(pointList.get(i).get("kp_id").toString());
                }
                // 查询完成数据对接的知识点
                Map<String, Object> map = new HashMap<>();
                map.put("kpIds", kpIds);
                List<Map> kpList = modelKpDao.getKpList(map);
            }
            return ApiResultHandler.buildApiResult(0, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }


    /**
     * @author: 胡立涛
     * @description: TODO 知识关系矩阵数据处理逻辑
     * @date: 2022/5/30
     * @param: []
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "relationProcess")
    public ApiResult relationProcess() {
        try {
            long userId = 1L;
            String knowledgeCode = "333";
            long deptId = 2L;
            long positionId = 3L;
            relationProcess(userId, knowledgeCode, deptId, positionId);
            return ApiResultHandler.buildApiResult(0, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }


    /**
     * @author: 胡立涛
     * @description: TODO 知识关系矩阵数据处理逻辑
     * @date: 2022/5/31
     * @param: [userId, knowledgeCode, deptId, positionId]
     * @return: void
     */
    public void relationProcess(Long userId, String knowledgeCode, Long deptId, Long positionId) throws Exception {
        // 从knowledge_view表中获取该用户访问过的所有知识code
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        // 查询近三天数据
        int dayNum = 3;
        String paramValue = manageBackendFeign.getParamValue(CommonConstans.day_num);
        if (StringUtils.isNotEmpty(paramValue)) {
            dayNum = Integer.parseInt(paramValue) - 1;
        }
        Calendar calendar1 = Calendar.getInstance();
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        calendar1.add(Calendar.DATE, 1);
        String endTime = sdf1.format(calendar1.getTime()) + " 23:59:59";
        Calendar calendar2 = Calendar.getInstance();
        calendar2.add(Calendar.DATE, -dayNum);
        String startTime = sdf1.format(calendar2.getTime()) + " 00:00:00";
        map.put("startTime", Timestamp.valueOf(startTime));
        map.put("endTime", Timestamp.valueOf(endTime));
        List<Map> knowledgeList = knowledgeViewDao.getKnowledgeList(map);
//        knowledgeList = CollectionsCustomer.builder().build().listMapToLowerCase(knowledgeList);
        if (knowledgeList != null && knowledgeList.size() > 0) {
            for (Map mBean : knowledgeList) {
                String tarCode = mBean.get("senses_id").toString();
                // 与当前知识建立关系
                // 根据知识code和关系知识查询数据
                map.put("knowledgeCode", knowledgeCode);
                map.put("relationKnowledge", tarCode);
                Map relationInfo = knowledgeRelationDao.getRelationInfo(map);
                if (relationInfo != null && relationInfo.size() > 0) {
                    //关系已经存在，对关联次数+1
                    KnowledgeRelation knowledgeRelation = knowledgeRelationDao.selectById(Long.valueOf(relationInfo.get("id").toString()));
                    knowledgeRelation.setNum(knowledgeRelation.getNum() + 1L);
                    knowledgeRelation.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                    knowledgeRelationDao.updateById(knowledgeRelation);
                } else {
                    KnowledgeRelation knowledgeRelation = new KnowledgeRelation();
                    knowledgeRelation.setKnowledgeCode(knowledgeCode);
                    knowledgeRelation.setRelationKnowledge(tarCode);
                    knowledgeRelation.setNum(1L);
                    knowledgeRelation.setCreateTime(new Timestamp(System.currentTimeMillis()));
                    knowledgeRelation.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                    knowledgeRelation.setDeptId(deptId);
                    knowledgeRelation.setPostionId(positionId);
                    knowledgeRelationDao.insert(knowledgeRelation);
                }
            }
        }
    }

    @Autowired
    KnowledgeFeign knowledgeFeign;

    /**
     * @author:胡立涛
     * @description: TODO 根据知识点id查询知识点树形菜单
     * @date: 2022/9/15
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getTreeByKpId")
    public ApiResult getTreeByKpId(@RequestBody Map<String, Object> map) {
        try {
            String kpId = map.get("kpId") == null ? null : map.get("kpId").toString();
            if (kpId == null) {
                return ApiResultHandler.buildApiResult(100, "参数为空", null);
            }
            // 根据kpid查询知识点树形菜单
            Map map1 = new HashMap();
            map1.put("ids", kpId);
            List<Map> knowledgeBindList = knowledgeFeign.getKnowledgeBind(map1);
            JSONArray result = listToTree(JSONArray.parseArray(JSON.toJSONString(knowledgeBindList)), "id", "parentid", "children");
            return ApiResultHandler.buildApiResult(200, "操作成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 机场模板右侧知识部分关系添加和更新
     * @date: 2022/9/15
     * @param: [airPort]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "/saveOrUpdateAirPort")
    public ApiResult saveOrUpdateAirPort(@RequestBody AirPort airPort) {
        try {
            if (StringUtils.isEmpty(airPort.getSourceId())) {
                return ApiResultHandler.buildApiResult(100, "参数sourceId为空", null);
            }
            if (StringUtils.isEmpty(airPort.getTargetId())) {
                return ApiResultHandler.buildApiResult(100, "参数targetId为空", null);
            }
            if (airPort.getKpId() == null) {
                return ApiResultHandler.buildApiResult(100, "参数kpId为空", null);
            }
            if (airPort.getOrginKid() == null) {
                return ApiResultHandler.buildApiResult(100, "参数orginKid为空", null);
            }
            if (airPort.getId() == null) {
                airPort.setCreateTime(new Timestamp(System.currentTimeMillis()));
                airPortDao.insert(airPort);
            } else {
                airPortDao.updateById(airPort);
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
     * @description: TODO 关系知识点中的属性数据存储（全球知识模板中的属性名称，经度，维度）
     * @date: 2022/9/19
     * @param: [relationKnowledgeData]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "saveRelationKnowledgeData")
    public ApiResult saveRelationKnowledgeData(@RequestBody RelationKnowledgeData relationKnowledgeData) {
        try {
            if (relationKnowledgeData.getModelDataId() == null) {
                return ApiResultHandler.buildApiResult(100, "参数modelDataId为空", null);
            }
            if (relationKnowledgeData.getProCode() == null) {
                return ApiResultHandler.buildApiResult(100, "参数proCode为空", null);
            }
            if (relationKnowledgeData.getAssemblyType() == null) {
                return ApiResultHandler.buildApiResult(100, "参数assemblyType为空", null);
            }
            relationKnowledgeData.setCreateTime(new Timestamp(System.currentTimeMillis()));
            relationKnowledgeDataDao.insert(relationKnowledgeData);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 知识关系数据查询
     * @date: 2022/9/19
     * @param: [modelKpId, modelId, knowledgeId, userId]
     * @return: com.cloud.core.ApiResult
     */
    @GetMapping("/getRelationKnowledge")
    public ApiResult getRelationKnowledge(@RequestParam(value = "modelKpId") String modelKpId,
                                          @RequestParam(value = "knowledgeId") String knowledgeId,
                                          @RequestParam(value = "userId") Integer userId) {
        try {
            Map rMap = new HashMap();
            ModelKp modelKp = modelKpService.getById(Integer.parseInt(modelKpId));
            if (modelKp == null) {
                return ApiResultHandler.buildApiResult(100, "不存在该知识模版信息", null);
            }
            // 根据知识点id查询知识名称
            Map point = knowledgeFeign.getKnowledgePointsById(modelKp.getKpId());
            // 知识点信息 收藏知识 关注知识点
            Map<String, Object> voKp = new HashMap<>();
            voKp.put("kpId", modelKp.getKpId());
            voKp.put("kpName", point.get("name"));
            voKp.put("collectStatus", getCollectState(userId, knowledgeId));
            voKp.put("constraintForce", getFocuState(userId, modelKp.getKpId()));
            voKp.put("classId", modelKp.getKpId());
            rMap.put("kpVO", voKp);
            // 查询模板类型
            Map<String, Object> pMap = new HashMap<>();
            pMap.put("modelKpId", Integer.parseInt(modelKpId));
            List<Map> nameList = modelDataDao.getAssemblyName(pMap);
            if (nameList != null && nameList.size() > 0) {
                for (Map map : nameList) {
                    String name = map.get("assembly_name").toString();
                    String id = map.get("id").toString();
                    // 根据assembly_name，modelKpId查询信息
                    pMap.put("assemblyName", name);
                    List<Map> proInfoList = modelDataDao.getProInfoList(pMap);
                    List<Map<String, Object>> rList = new ArrayList<>();
                    for (Map map_1 : proInfoList) {
                        String proCode = map_1.get("pro_code").toString();
                        String sqlStr = map_1.get("sql_str") == null ? null : map_1.get("sql_str").toString();
                        String proTypeCode = map_1.get("pro_type_code").toString();
                        Integer modelDataId = Integer.parseInt(map_1.get("id").toString());
                        Map knowledgeParMap = new HashMap();
                        knowledgeParMap.put("proTypeCode", proTypeCode);
                        knowledgeParMap.put("knowledgeId", knowledgeId);
                        knowledgeParMap.put("proCode", proCode);
                        List<Map> knowledgModelStepOne = knowledgeFeign.getKnowledgModelStepOne(knowledgeParMap);
                        if (knowledgModelStepOne != null && knowledgModelStepOne.size() > 0) {
                            for (Map detail : knowledgModelStepOne) {
                                Map knowledgeMap = new HashMap();
                                String knowledgeCode = detail.get("knowledge_id").toString();
                                knowledgeMap.put("knowledgeCode", knowledgeCode);
                                knowledgeMap.put("kpId", detail.get("point_id"));
                                knowledgeMap.put("kpName", detail.get("point_name"));
                                knowledgeMap.put("knowledgeName", detail.get("knowledge_name"));
                                knowledgeMap.put("sqlstr", sqlStr);
                                QueryWrapper queryWrapper = new QueryWrapper();
                                Map parMap = new HashMap();
                                parMap.put("kpCode", proCode);
                                parMap.put("modelDataId", modelDataId);
                                List<Map<String, Object>> list = relationKnowledgeDataDao.getList(parMap);
                                List list2 = new ArrayList();
                                if (list != null && list.size() > 0) {
                                    for (Map d2 : list) {
                                        knowledgeParMap = new HashMap();
                                        knowledgeParMap.put("knowledgeId", knowledgeCode);
                                        knowledgeParMap.put("proId", d2.get("pro_code"));
                                        List<Map> proValueList = knowledgeFeign.getProValue(knowledgeParMap);
                                        if (proValueList != null && proValueList.size() > 0) {
                                            for (Map d3 : proValueList) {
                                                Map map2 = new HashMap();
                                                map2.put("proProname", d3.get("name"));
                                                map2.put("proProvalue", d3.get("data_property_value"));
                                                list2.add(map2);
                                            }
                                        }
                                    }
                                }
                                knowledgeMap.put("proList", list2);
                                rList.add(knowledgeMap);
                            }
                        }
                    }
                    rMap.put(name, rList);
                }
            }
            return ApiResultHandler.buildApiResult(200, "知识关系数据查询成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "知识关系数据查询异常：" + e.toString(), null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据id删除知识关系数据属性信息
     * @date: 2022/9/21
     * @param: [relationKnowledgeData]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "delRelationKnowledgeData")
    public ApiResult delRelationKnowledgeData(@RequestBody RelationKnowledgeData relationKnowledgeData) {
        try {
            if (relationKnowledgeData.getId() == null || relationKnowledgeData.getId() == 0l) {
                return ApiResultHandler.buildApiResult(100, "参数id为空：", null);
            }
            relationKnowledgeDataDao.deleteById(relationKnowledgeData.getId());
            return ApiResultHandler.buildApiResult(200, "操作成功：", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "删除知识关系数据属性信息异常：" + e.toString(), null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 机场模板右侧知识部分关系添查询
     * @date: 2022/9/22
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getAirPortList")
    public ApiResult getAirPortList(@RequestBody Map<String, Object> map) {
        try {
            if (map.get("kpId") == null) {
                return ApiResultHandler.buildApiResult(100, "参数kpId为空", null);
            }
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("kp_id", Long.valueOf(map.get("kpId").toString()));
            List list = airPortDao.selectList(queryWrapper);
            return ApiResultHandler.buildApiResult(200, "操作成功", list);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据id删除机场模板右侧知识部分关系数据
     * @date: 2022/9/22
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "delAirPort")
    public ApiResult delAirPort(@RequestBody Map<String, Object> map) {
        try {
            if (map.get("id") == null) {
                return ApiResultHandler.buildApiResult(100, "参数id为空", null);
            }
            airPortDao.deleteById(Long.valueOf(map.get("id").toString()));
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 复杂模板 资源数据查询
     * @date: 2022/9/26
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getResourcesByPar")
    public ApiResult getResourcesByPar(@RequestBody Map<String, Object> map) {
        try {
            String knowledgeId = map.get("knowledgeId") == null ? null : map.get("knowledgeId").toString();
            if (knowledgeId == null) {
                return ApiResultHandler.buildApiResult(100, "参数knowledgeId为空", null);
            }
            if (map.get("modelKpId") == null) {
                return ApiResultHandler.buildApiResult(100, "参数modelKpId为空", null);
            }
            QueryWrapper<ModelData> queryWrapper = new QueryWrapper();
            queryWrapper.eq("model_kp_id", Long.valueOf(map.get("modelKpId").toString()));
            queryWrapper.eq("constraint_force", "0");
            if (map.get("sqlStr") != null) {
                queryWrapper.eq("sql_str", map.get("sqlStr").toString());
            }
            List<ModelData> list = modelDataDao.selectList(queryWrapper);
            List<Map> rList = new ArrayList<>();
            for (ModelData bean : list) {
                String proTypeCode = bean.getProTypeName();
                String proCode = bean.getProProname();
                // 关系
                // 根据 知识点id，知识id，关系id，关联概念id，查询关系数据
                Map m = new HashMap();
                m.put("proId", proCode);
                m.put("proTypeCode", proTypeCode);
                m.put("knowledgeId", knowledgeId);
                List<Map> relationProVal = knowledgeFeign.getRelationProVal(m);
                List<Map> imageList = new ArrayList<>();
                String imageAddress = "";
                String imageName = "";
                for (Map detail : relationProVal) {
                    String data_type = detail.get("data_type") == null ? "" : detail.get("data_type").toString();
                    if (data_type != "" && data_type.equals("IMAGE")) {
                        imageAddress += tupuPicServer + detail.get("data_property_value").toString() + ";";
                    } else {
                        imageName = detail.get("data_property_value").toString();
                    }
                }
                Map imageMap = new HashMap();
                imageMap.put("imageName", imageName);
                imageMap.put("imageUrl", imageAddress == "" ? "" : imageAddress.substring(0, imageAddress.length() - 1));
                imageList.add(imageMap);
                Map m1 = new HashMap();
                m1.put("list", imageList);
                m1.put("proProname", "tupian");
                rList.add(m1);
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", rList);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 复杂模板 删除子项信息
     * @date: 2022/9/26
     * @param: [modelControl]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "delModelControl")
    public ApiResult delModelControl(@RequestBody ModelControl modelControl) {
        try {
            if (modelControl.getId() == null) {
                return ApiResultHandler.buildApiResult(100, "参数id为空", null);
            }
            if (modelControl.getModelKpId() == null) {
                return ApiResultHandler.buildApiResult(100, "参数modelKpId为空", null);
            }
            modelControlService.delModelControl(modelControl);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 影像标注模板：查询知识点下的知识列表（带是否完成数据对接标识）
     * @date: 2022/10/20
     * @param: [parMap]
     * @return: com.cloud.core.ApiResult
     */
//    @PostMapping(value = "getKnowledgeList")
//    public ApiResult getKnowledgeList(@RequestBody Map<String, Object> parMap) {
//        try {
//            Long kpId = parMap.get("kpId") == null ? null : Long.valueOf(parMap.get("kpId").toString());
//            if (kpId == null) {
//                return ApiResultHandler.buildApiResult(100, "参数kpId为空", null);
//            }
//            KnowledgePoints knowledgePointsById = manageBackendFeign.getKnowledgePointsById(kpId);
//            String kpCode = knowledgePointsById.getCode();
//            List<Map> rList = new ArrayList<>();
//            String kpUrl = tupuServer + CommonConstans.getAllKnowledgeListPoint + "?kpCode=" + kpCode;
//            String esKnowledgeString = getOther(kpUrl);
//            if (StringUtils.isNotEmpty(esKnowledgeString)) {
//                JSONObject esObject = JSON.parseObject(esKnowledgeString);
//                String code = esObject.getString("code");
//                if (code.equals("200")) {
//                    List<Map> result = (List) esObject.get("result");
//                    for (Map map : result) {
//                        Map m = new HashMap();
//                        String knowledgeCode = map.get("id").toString();
//                        // 查看该知识是否完成数据对接
//                        QueryWrapper queryWrapper = new QueryWrapper();
//                        queryWrapper.eq("kp_id", kpId);
//                        queryWrapper.eq("knowledge_code", knowledgeCode);
//                        List list = knowledgeTifDao.selectList(queryWrapper);
//                        m.put("flg", 0);
//                        if (!list.isEmpty() && list.size() > 0) {
//                            m.put("flg", 1);
//                        }
//                        m.put("knowledgeCode", knowledgeCode);
//                        m.put("knowledgeName", map.get("name"));
//                        rList.add(m);
//                    }
//                } else {
//                    return ApiResultHandler.buildApiResult(500, "第三方接口异常", null);
//                }
//            }
//            return ApiResultHandler.buildApiResult(ResultMesEnum.SUCCESS.getResultCode(), "查询知识点下的知识列表（带是否完成数据对接标识）成功", rList);
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error(e.toString());
//            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
//        }
//    }

    /**
     * @author:胡立涛
     * @description: TODO 影像标注模板：保存按钮逻辑
     * @date: 2022/10/20
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "saveInfo")
    public ApiResult saveInfo(@RequestBody Map<String, Object> map) {
        try {
            String knowledgeCode = map.get("knowledgeCode") == null ? null : map.get("knowledgeCode").toString();
            String proCode = map.get("proCode") == null ? null : map.get("proCode").toString();
            String proValue = map.get("proValue") == null ? null : map.get("proValue").toString();
            if (knowledgeCode == null) {
                return ApiResultHandler.buildApiResult(100, "参数knowledgeCode为空", null);
            }
            if (proCode == null) {
                return ApiResultHandler.buildApiResult(100, "参数proCode为空", null);
            }
            if (proValue == null) {
                return ApiResultHandler.buildApiResult(100, "参数proValue为空", null);
            }
            // 调用图谱接口，进行属性更新
            String kpUrl = tupuServer + CommonConstans.saveProInfo + "?sensId =" + knowledgeCode + "&proCode=" + proCode + "proValue" + proValue;
            String esKnowledgeString = getOther(kpUrl);
            JSONObject esObject = JSON.parseObject(esKnowledgeString);
            String code = esObject.getString("code");
            if (code.equals("200")) {
                return ApiResultHandler.buildApiResult(200, "保存图层操作成功", null);
            }
            return ApiResultHandler.buildApiResult(100, "调用图谱保存接口异常", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据属性名称，属性值，知识点名称查询知识列表，知识ids查询知识集合
     * @date: 2024/8/1
     * @param: [map] proCode、proValue、knowledges、calssIds、list（属性和属性值集合）
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getKnowledgeByProCode")
    public ApiResult getKnowledgeByProCode(@RequestBody Map<String, Object> map) {
        try {
            Map parMap = new HashMap();
            // 知识点ids
            String classNames = map.get("classNames") == null ? "" : map.get("classNames").toString();
            List<String> kpIds = null;
            if (classNames != "" && classNames.trim().length() > 0) {
                List<String> classNameList = new ArrayList<>();
                String[] classArr = classNames.split(",");
                for (int i = 0; i < classArr.length; i++) {
                    classNameList.add(classArr[i]);
                }
                kpIds = new ArrayList<>();
                Map m = new HashMap();
                m.put("classNames", classNameList);
                List<Map> points = knowledgeFeign.getPoints(m);
                if (points == null || points.size() == 0) {
                    return ApiResultHandler.buildApiResult(200, "数据库中不存在知识点 " + classNames, null);
                }
                for (Map point : points) {
                    kpIds.add(point.get("id").toString());
                }
            }
            parMap.put("kpIds", kpIds);
            // 知识ids
            String knowledges = map.get("knowledges") == null ? "" : map.get("knowledges").toString();
            List<String> knowledgeIds = null;
            if (knowledges != "" && knowledges.trim().length() > 0) {
                knowledgeIds = new ArrayList<>();
                String[] knowledgeArr = knowledges.split(",");
                for (int i = 0; i < knowledgeArr.length; i++) {
                    knowledgeIds.add(knowledgeArr[i]);
                }
            }
            parMap.put("knowledgeIds", knowledgeIds);
            List<Map> rList = null;
            // 属性code和属性值
            if (map.get("list") != null && map.get("list") != "") {
                List<Map> list = (List<Map>) map.get("list");
                for (int i = 0; i < list.size(); i++) {
                    Map bean = list.get(i);
                    if (bean.get("proValue") == null) {
                        continue;
                    }
                    Map proName = knowledgeFeign.getProCode(bean.get("proName").toString());
                    if (proName == null) {
                        return ApiResultHandler.buildApiResult(101, "数据库中无该属性：" + bean.get("proName"), rList);
                    }
                    parMap.put("proCode", proName.get("id"));
                    parMap.put("proValue", bean.get("proValue"));
                    parMap.put("listFlg", bean.get("listFlg"));
                    if (bean.get("listFlg") != null) {
                        int listFlg = Integer.parseInt(bean.get("listFlg").toString());
                        if (listFlg == 1) {
                            List<String> vList = new ArrayList<>();
                            String[] proValueArr = bean.get("proValue").toString().split(",");
                            for (int k = 0; k < proValueArr.length; k++) {
                                vList.add(proValueArr[k]);
                            }
                            parMap.put("vList", vList);
                        }
                        if (listFlg == 2) {
                            int wc = Integer.parseInt(bean.get("wc").toString());
                            int proValue = Integer.parseInt(bean.get("proValue").toString());
                            String startNum = String.valueOf(proValue - wc);
                            String endNum = String.valueOf(proValue - wc);
                            parMap.put("startNum", startNum);
                            parMap.put("endNum", endNum);
                        }

                    }
                    rList = new ArrayList<>();
                    rList = knowledgeFeign.getKnowledgeByProCode(parMap);
                    if (rList == null || rList.size() == 0) {
                        return ApiResultHandler.buildApiResult(200, "操作成功", rList);
                    }
                    if (rList != null && rList.size() > 0) {
                        knowledgeIds = new ArrayList<>();
                        for (Map knowledge : rList) {
                            knowledgeIds.add(knowledge.get("knowledge_id").toString());
                        }
                        parMap.put("knowledgeIds", knowledgeIds);
                    }
                }
            } else {
                rList = knowledgeFeign.getKnowledgeByProCode(parMap);
            }
            List<Map> list = new ArrayList<>();
            if (rList != null && rList.size() > 0) {
                for (Map bean : rList) {
                    String kpId = bean.get("point_id").toString();
                    String knowledgeId = bean.get("knowledge_id").toString();
                    String knowledgeName = bean.get("knowledge_name").toString();
                    // 封面图
                    Map proName = knowledgeFeign.getProCode("封面图片");
                    String proCode = proName.get("id").toString();
                    Map photoMap = new HashMap();
                    photoMap.put("knowledgeId", knowledgeId);
                    photoMap.put("proId", proCode);
                    Map mm = knowledgeFeign.getPhoto(photoMap);
                    String photo = "";
                    if (mm != null) {
                        photo = tupuPicServer + mm.get("data_property_value");
                    }
                    Map m = new HashMap();
                    m.put("photo", photo);
                    m.put("kpId", kpId);
                    m.put("knowledgeId", knowledgeId);
                    m.put("knowledgeName", knowledgeName);
                    list.add(m);
                }
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", list);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO deepseek 根据知识名称查询知识信息
     * @date: 2024/9/5
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "deepseekKnowledge")
    public ApiResult deepseekKnowledge(@RequestBody Map<String, Object> map) {
        try {
            Map rMap = new HashMap();
            Map m = knowledgeFeign.deepseekKnowlege(map);
            if (m != null) {
                rMap.put("kpId", m.get("kp_id"));
                rMap.put("knowledgeId", m.get("knowledge_id"));
                rMap.put("knowledgeName", m.get("knowledge_name"));
                Map proName = knowledgeFeign.getProCode("封面图片");
                String proCode = proName.get("id").toString();
                Map photoMap = new HashMap();
                photoMap.put("knowledgeId", m.get("knowledge_id"));
                photoMap.put("proId", proCode);
                Map mm = knowledgeFeign.getPhoto(photoMap);
                String photo = tupuPicServer + mm.get("data_property_value");
                rMap.put("photo", photo);
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 舰载武器查询（根据知识点，生产国家查询知识数据）
     * @date: 2024/8/2
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getRelationKnowledgesByParam")
    public ApiResult getRelationKnowledgesByParam(@RequestBody Map map) {
        try {
            Map parMap = new HashMap();
            // 知识点ids
            String classIds = map.get("calssIds") == null ? "" : map.get("calssIds").toString();
            List<String> kpIds = null;
            if (classIds != "" && classIds.trim().length() > 0) {
                kpIds = new ArrayList<>();
                String[] calssIdArr = classIds.split(",");
                for (int i = 0; i < calssIdArr.length; i++) {
                    kpIds.add(calssIdArr[i]);
                }
            }
            parMap.put("kpIds", kpIds);
            // 国家
            String countrys = map.get("countrys") == null ? "" : map.get("countrys").toString();
            List<String> countryList = null;
            if (countrys != "" && countrys.trim().length() > 0) {
                countryList = new ArrayList<>();
                String[] calssIdArr = countrys.split(",");
                for (int i = 0; i < calssIdArr.length; i++) {
                    countryList.add(calssIdArr[i]);
                }
            }
            parMap.put("countryList", countryList);
            List<Map> list = knowledgeFeign.getRelationKnowledgesByParam(parMap);
            List<Map> rList = new ArrayList<>();
            if (list != null && list.size() > 0) {
                for (Map bean : list) {
                    String kpId = bean.get("point_id").toString();
                    String knowledgeId = bean.get("knowledge_id").toString();
                    String knowledgeName = bean.get("knowledge_name").toString();
                    // 封面图片获取
                    String photo = "";
                    Map photoMap = knowledgeFeign.getPhotoImage(knowledgeId);
                    if (photoMap != null) {
                        photo = photoMap.get("data_property_value") == null ? "" : photoMap.get("data_property_value").toString();
                        if (photo != "" && photo.trim().length() > 0) {
                            photo = tupuPicServer + photo;
                        }
                    }
                    // 影像图片获取
                    String tifImage = "";
                    Map tifMap = knowledgeFeign.getTifImage(knowledgeId);
                    if (tifMap != null) {
                        tifImage = tifMap.get("data_property_value") == null ? "" : tifMap.get("data_property_value").toString();
                        if (tifImage != "" && tifImage.trim().length() > 0) {
                            tifImage = tupuPicServer + tifImage;
                        }
                    }
                    Map m = new HashMap();
                    m.put("photo", photo);
                    m.put("tifImage", tifImage);
                    m.put("kpId", kpId);
                    m.put("knowledgeId", knowledgeId);
                    m.put("knowledgeName", knowledgeName);
                    rList.add(m);
                }
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", rList);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 舰载武器：根据前甲板，后甲板查询数据
     * @date: 2024/8/5
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getRelationProperty")
    public ApiResult getRelationProperty(@RequestBody Map map) {
        try {
            List<Map> rList = new ArrayList<>();
            String[] mainKnowledges = map.get("mainKnowledges").toString().split(",");
            List<String> knowledgeIds = new ArrayList<>();
            for (int i = 0; i < mainKnowledges.length; i++) {
                knowledgeIds.add(mainKnowledges[i]);
            }
            List<Map> knowledges = (List<Map>) map.get("knowledges");
            List<Map> relationPropertyList = null;
            List<Map> newList = null;
            for (int i = 0; i < knowledges.size(); i++) {
                Map parMap = new HashMap();
                Map detail = knowledges.get(i);
                String knowledgeId = detail.get("knowledgeId").toString();
                Integer befor = detail.get("befor").toString() == "" ? 0 : Integer.parseInt(detail.get("befor").toString());
                Integer after = detail.get("after").toString() == "" ? 0 : Integer.parseInt(detail.get("after").toString());
                Integer middle = detail.get("middle").toString() == "" ? 0 : Integer.parseInt(detail.get("middle").toString());
                parMap.put("targetKnowledgeId", knowledgeId);
                parMap.put("knowledgeIds", knowledgeIds);
                relationPropertyList = knowledgeFeign.getRelationProperty(parMap);
                newList = new ArrayList<>();
                knowledgeIds = new ArrayList<>();
                if (relationPropertyList != null && relationPropertyList.size() > 0) {
                    for (Map proDetail : relationPropertyList) {
                        String domainKnowledge = proDetail.get("knowledge_id").toString();
                        String metaValue = proDetail.get("meta_properties") == null ? "" : proDetail.get("meta_properties").toString();
                        if (metaValue == "" || metaValue.trim().length() == 0 || metaValue.equals("null")) {
                            if (befor == 0 && after == 0 && middle == 0) {
                                knowledgeIds.add(domainKnowledge);
                                newList.add(detail);
                            }
                        } else {
                            newList.add(proDetail);
                            List<RelationBean> metaList = JSON.parseArray(proDetail.get("meta_properties").toString(), RelationBean.class);
                            int index = 0;
                            for (RelationBean metaDetail : metaList) {
                                String label = metaDetail.getLabel() == null ? "" : metaDetail.getLabel().toString();
                                String value = metaDetail.getValue() == null ? "" : metaDetail.getValue().toString();
                                if (label != "" && label.trim().length() > 0) {
                                    if (label.equals("befor")) {
                                        Integer val = value == "" ? 0 : Integer.valueOf(value);
                                        if (befor <= val) {
                                            index += 1;
                                        }
                                    }
                                    if (label.equals("after")) {
                                        Integer val = value == "" ? 0 : Integer.valueOf(value);
                                        if (after <= val) {
                                            index += 1;
                                        }
                                    }
                                    if (label.equals("middle")) {
                                        Integer val = value == "" ? 0 : Integer.valueOf(value);
                                        if (middle <= val) {
                                            index += 1;
                                        }
                                    }
                                }
                            }
                            if (index == 3) {
                                knowledgeIds.add(domainKnowledge);
                            }
                        }
                    }
                    if (knowledgeIds.size() == 0) {
                        return ApiResultHandler.buildApiResult(200, "操作成功", rList);
                    }
                } else {
                    return ApiResultHandler.buildApiResult(200, "操作成功", rList);
                }
            }
            for (Map detail : newList) {
                Map rMap = new HashMap();
                // 封面图片获取
                Map proName = knowledgeFeign.getProCode("封面图片");
                String proCode = proName.get("id").toString();
                Map photoMap = new HashMap();
                photoMap.put("knowledgeId", detail.get("knowledge_id"));
                photoMap.put("proId", proCode);
                Map mm = knowledgeFeign.getPhoto(photoMap);
                String photo = tupuPicServer + mm.get("data_property_value");
                Map m = new HashMap();
                m.put("photo", photo);
                rMap.put("photo", photo);
                rMap.put("knowledgeId", detail.get("knowledge_id"));
                rMap.put("kpId", detail.get("point_id"));
                rMap.put("knowledgeName", detail.get("knowledge_name"));
                rList.add(rMap);
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", rList);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 战斗舰艇判读所需对比图片查询
     * @date: 2024/8/6
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getPics")
    public ApiResult getPics(@RequestBody Map map) {
        try {
            String knowledgeOne = map.get("knowledgeOne").toString();
            String knowledgeTwo = map.get("knowledgeTwo").toString();
            String weizhis = map.get("weizhis") == null ? "" : map.get("weizhis").toString();
            String jiaodus = map.get("jiaodus") == null ? "" : map.get("jiaodus").toString();
            if (weizhis == "" || weizhis.trim().length() == 0) {
                return ApiResultHandler.buildApiResult(200, "参数weizhis为空", null);
            }
            if (jiaodus == "" || jiaodus.trim().length() == 0) {
                return ApiResultHandler.buildApiResult(200, "参数jiaodus为空", null);
            }
            String[] weizhiArr = weizhis.split(",");
            String[] jiaoduArr = jiaodus.split(",");
            List<Map> rList = new ArrayList<>();
            for (int i = 0; i < weizhiArr.length; i++) {
                String weizhi = weizhiArr[i];
                String weizhiName = getDictionaryName(weizhi);
                for (int k = 0; k < jiaoduArr.length; k++) {
                    Map m = new HashMap();
                    String jiaodu = jiaoduArr[k];
                    String jiaoduName = getDictionaryName(jiaodu);
                    Map parMap = new HashMap();
                    parMap.put("knowledgeId", knowledgeOne);
                    parMap.put("jiaodu", jiaodu);
                    parMap.put("weizhi", weizhi);
                    List<Map> pics = knowledgeFeign.getPics(parMap);
                    String name = weizhiName + "-" + jiaoduName;
                    m.put("name", name);
                    m.put("knowledgeOne", pics);
                    parMap = new HashMap();
                    parMap.put("knowledgeId", knowledgeTwo);
                    parMap.put("jiaodu", jiaodu);
                    parMap.put("weizhi", weizhi);
                    pics = knowledgeFeign.getPics(parMap);
                    m.put("knowledgeTwo", pics);
                    rList.add(m);
                }
            }
            Map rMap = new HashMap();
            rMap.put("tupuPicServer", tupuPicServer);
            rMap.put("list", rList);
            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    public String getDictionaryName(String code) {
        String name = "";
        Map sysDictionary = knowledgeFeign.getSysDictionary(code);
        if (sysDictionary != null) {
            name = sysDictionary.get("name") == null ? "" : sysDictionary.get("name").toString();
        }
        return name;
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据属性名称查询属性code值
     * @date: 2024/8/14
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getProCode")
    public ApiResult getProCode(@RequestBody Map map) {
        try {
            Map name = knowledgeFeign.getProCode(map.get("name").toString());
            return ApiResultHandler.buildApiResult(200, "操作成功", name);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据知识点名称查询知识点code
     * @date: 2024/8/14
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getPointCode")
    public ApiResult getPointCode(@RequestBody Map map) {
        try {
            Map name = knowledgeFeign.getPointCode(map.get("name").toString());
            return ApiResultHandler.buildApiResult(200, "操作成功", name);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 查询舰载武器知识点列表
     * @date: 2024/8/15
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getwqPoint")
    public ApiResult getwqPoint(@RequestBody Map map) {
        try {
            List<Map> list = knowledgeFeign.getwqPoint(map.get("name").toString());
            return ApiResultHandler.buildApiResult(200, "操作成功", list);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 查询某知识的所有关系图片
     * @date: 2024/8/19
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getKnowledgePics")
    public ApiResult getKnowledgePics(@RequestBody Map map) {
        try {
            List<Map> knowledgeId = knowledgeFeign.getKnowledgePics(map.get("knowledgeId").toString());
            Map rMap = new HashMap();
            rMap.put("list", knowledgeId);
            rMap.put("tupuPicServer", tupuPicServer);
            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 舰面符号、烟囱、舰艏、水平舵下拉框及图片获取
     * @date: 2024/8/21
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getmjImage")
    public ApiResult getmjImage(@RequestBody Map map) {
        try {
            List<Map> list = knowledgeFeign.getmjImage(map.get("typeId").toString());
            Map rMap = new HashMap();
            rMap.put("list", list);
            rMap.put("tupuPicServer", tupuPicServer);
            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 舰面符号查询逻辑
     * @date: 2024/8/28
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getxianhao")
    public ApiResult getxianhao(@RequestBody Map map) {
        try {
            List list = new ArrayList();
            List<Map> xianhaoList = knowledgeFeign.getxianhao(map);
            if (xianhaoList != null && xianhaoList.size() > 0) {
                for (Map bean : xianhaoList) {
                    String kpId = bean.get("point_id").toString();
                    String knowledgeId = bean.get("knowledge_id").toString();
                    String knowledgeName = bean.get("knowledge_name").toString();
                    // 封面图
                    QueryWrapper query = new QueryWrapper();
                    query.eq("kp_id", kpId);
                    KnowledgePic knowledgePic = knowledgePicDao.selectOne(query);
                    String photo = "";
                    if (knowledgePic != null) {
                        Map picMap = new HashMap();
                        picMap.put("knowledgeId", knowledgeId);
                        picMap.put("proId", knowledgePic.getProId());
                        List<Map> proValue = knowledgeFeign.getProValue(picMap);
                        if (proValue != null && proValue.size() > 0) {
                            photo = tupuPicServer + proValue.get(0).get("data_property_value");
                        }
                    }
                    Map m = new HashMap();
                    m.put("photo", photo);
                    m.put("kpId", kpId);
                    m.put("knowledgeId", knowledgeId);
                    m.put("knowledgeName", knowledgeName);
                    list.add(m);
                }
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", list);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }
}
