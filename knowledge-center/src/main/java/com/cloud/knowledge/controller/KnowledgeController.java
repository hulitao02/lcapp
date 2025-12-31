package com.cloud.knowledge.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.knowledge.dao.*;
import com.cloud.knowledge.model.*;
import com.cloud.knowledge.service.KnowledgeService;
import com.cloud.model.user.LoginAppUser;
import com.cloud.model.utils.AppUserUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.matcher.EqualityMatcher;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.common.collect.HppcMaps;
import org.elasticsearch.indices.recovery.RecoveryState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import util.ZipUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@RestController
@ApiModel(value = "从图谱数据库获取数据")
@Slf4j
@RefreshScope
public class KnowledgeController {

    @Autowired
    KnowledgeDao knowledgeDao;
    // 图谱图片访问地址
    @Value(value = "${tupu_pic_server}")
    private String tupuPicServer;

    /**
     * @author:胡立涛
     * @description: TODO 查询所有知识点，返回树形菜单
     * @date: 2024/7/16
     * @param: []
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "/knowledgepoints/all")
    public ApiResult getKnowledgePointList() {
        try {
            List<OntologyClass> oldList = new ArrayList<>();
            List<OntologyClass> list = new ArrayList<>();
            List<Map> tree = knowledgeDao.getTree();
            if (tree != null && tree.size() > 0) {
                for (Map map : tree) {
                    OntologyClass bean = new OntologyClass();
                    bean.setId(map.get("id").toString());
                    bean.setPointName(map.get("pointname") == null ? "" : map.get("pointname").toString());
                    String parentId = map.get("parentid").toString();
                    if (parentId.equals("") || parentId.trim().length() == 0) {
                        bean.setParentId("0");
                    } else {
                        bean.setParentId(map.get("parentid").toString());
                    }
                    oldList.add(bean);
                }
                setSortTable("0", oldList, list);
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
     * @description: TODO 获取根节点下所有知识点
     * @date: 2024/7/17
     * @param: []
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getKnowledgePointListFeign")
    public List<Map> getKnowledgePointListFeign() throws Exception {
        return knowledgeDao.getTree();
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据知识点id查询知识点信息
     * @date: 2024/7/17
     * @param: [id]
     * @return: com.cloud.knowledge.model.OntologyClass
     */
    @GetMapping("/getKnowledgePointsById/{id}")
    public Map getKnowledgePointsById(@PathVariable String id) {

        return knowledgeDao.getKnowledgePointsById(id);
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据知识点id查询知识点分组及分组下的属性信息
     * @date: 2024/7/17
     * @param: [id]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping("/getProListByPointId/{id}")
    public List<Map> getProListByPointId(@PathVariable String id) throws Exception {
        List<Map> kpGroupList = knowledgeDao.getKpGroup(id);
        List gList = new ArrayList();
        if (kpGroupList != null && kpGroupList.size() > 0) {
            for (Map map : kpGroupList) {
                String groupId = map.get("id").toString();
                String groupName = map.get("name").toString();
                Map gMap = new HashMap();
                gMap.put("id", groupId);
                gMap.put("isProperty", 2);
                gMap.put("proTypeName", groupName);
                // 查询分组下的属性
                Map pMap = new HashMap();
                pMap.put("kpId", id);
                pMap.put("groupId", groupId);
                List<Map> groupProList = knowledgeDao.getGroupPro(pMap);
                List<Map> proList = null;
                if (groupProList != null && groupProList.size() > 0) {
                    proList = new ArrayList<>();
                    for (Map bean : groupProList) {
                        Map detailMap = new HashMap();
                        detailMap.put("id", bean.get("id").toString());
                        detailMap.put("isProperty", 1);
                        detailMap.put("kpId", id);
                        detailMap.put("kpIdString", id);
                        detailMap.put("proCode", bean.get("id").toString());
                        detailMap.put("proProname", bean.get("name").toString());
                        detailMap.put("proTypeCode", groupId);
                        detailMap.put("proTypeName", groupName);
                        detailMap.put("status", 1);
                        detailMap.put("childrenList", null);
                        proList.add(detailMap);
                    }
                }
                gMap.put("childrenList", proList);
                gList.add(gMap);
            }
        }
        return gList;
    }


    /**
     * @author:胡立涛
     * @description: TODO 查询知识点下的所有知识
     * @date: 2024/7/17
     * @param: [kpId]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping("/getAllKnowledgeListByPointId/{kpId}")
    List<Map> getAllKnowledgeListByPointId(@PathVariable("kpId") String kpId) throws Exception {
        return knowledgeDao.getAllKnowledgeListByPointId(kpId);
    }

    /**
     * @author:胡立涛
     * @description: TODO 获取根节点信息
     * @date: 2024/7/17
     * @param: []
     * @return: java.util.Map
     */
    @GetMapping(value = "/knowledgepoints/getRootPointFeign")
    public Map getRootPoint() throws Exception {
        return knowledgeDao.getRootPoint();
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据知识点ids查询树形菜单
     * @date: 2024/7/17
     * @param: [ids]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getKnowledgeBindFeign")
    public List<Map> getKnowledgeBind(@RequestBody Map map) throws Exception {
        String ids = map.get("ids").toString();
        String[] idsarr = ids.split(",");
        List<String> idsList = new ArrayList<>();
        for (int i = 0; i < idsarr.length; i++) {
            idsList.add(idsarr[i].trim());
        }
        return knowledgeDao.getKnowledgeBind(idsList);
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据知识点id，属性id查询属性详细信息
     * @date: 2024/7/17
     * @param: [map]
     * @return: java.util.Map
     */
    @PostMapping(value = "/knowledgepoints/getProDetail")
    public Map getProDetail(@RequestBody Map map) throws Exception {
        return knowledgeDao.getProDetail(map);
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据知识id，属性id查询属性对应的值
     * @date: 2024/7/18
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getProValue")
    public List<Map> getProValue(@RequestBody Map map) throws Exception {
        return knowledgeDao.getProValue(map);
    }

    /**
     * @author:胡立涛
     * @description: TODO 获取封面图片
     * @date: 2024/8/14
     * @param: [map]
     * @return: java.util.Map
     */
    @PostMapping(value = "/knowledgepoints/getPhoto")
    public Map getPhoto(@RequestBody Map map) throws Exception {
        return knowledgeDao.getProByPar(map);
    }


    /**
     * @author:胡立涛
     * @description: TODO 推荐知识列表
     * @date: 2024/7/18
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/listKnowledgePage")
    List<Map> listKnowledgePage(@RequestBody Map map) throws Exception {
        return knowledgeDao.listKnowledgePage(map);
    }


    /**
     * @author:胡立涛
     * @description: TODO 推荐知识列表总条数
     * @date: 2024/7/18
     * @param: [map]
     * @return: java.lang.Integer
     */
    @PostMapping(value = "/knowledgepoints/listKnowledgePageCount")
    Integer listKnowledgePageCount(@RequestBody Map map) throws Exception {
        Map m = knowledgeDao.listKnowledgePageCount(map);
        if (m.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(m.get("count").toString());
    }

    private void setSortTable(String parentId, List<OntologyClass> all, List<OntologyClass> list) {
        all.forEach(a -> {
            if (a.getParentId().equals(parentId)) {
                list.add(a);
                setSortTable(a.getId(), all, list);
            }
        });
    }

    /**
     * @author:胡立涛
     * @description: TODO 查询关系分组
     * @date: 2024/7/22
     * @param: [kpId]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping("/knowledgepoints/getRelationGroup/{kpId}")
    List<Map> getRelationGroup(@PathVariable("kpId") String kpId) throws Exception {
        return knowledgeDao.getRelationGroup(kpId);
    }


    /**
     * @author:胡立涛
     * @description: TODO 查询关系分组下的属性
     * @date: 2024/7/22
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getRelationPro")
    List<Map> getRelationPro(@RequestBody Map map) throws Exception {
        return knowledgeDao.getRelationPro(map);
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据关系id查询关系信息
     * @date: 2024/7/23
     * @param: [id]
     * @return: java.util.Map
     */
    @GetMapping("/knowledgepoints/getRelationGroupById/{id}")
    Map getRelationGroupById(@PathVariable("id") String id) throws Exception {
        return knowledgeDao.getRelationGroupById(id);
    }

    /**
     * @author:胡立涛
     * @description: TODO 查询关系中图片的值
     * @date: 2024/7/23
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getRelationProVal")
    List<Map> getRelationProVal(@RequestBody Map map) throws Exception {
        return knowledgeDao.getRelationProVal(map);
    }

    /**
     * @author:胡立涛
     * @description: TODO 关系：查询概念下的知识
     * @date: 2024/7/25
     * @param: [classId]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping("/knowledgepoints/getStepOne/{classId}")
    List<Map> getStepOne(@PathVariable("classId") String classId) {
        return knowledgeDao.getStepOne(classId);
    }

    /**
     * @author:胡立涛
     * @description: TODO 关系：查询概念下实际关联的知识
     * @date: 2024/7/25
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getStepTwo")
    List<Map> getStepTwo(@RequestBody Map map) {
        return knowledgeDao.getStepTwo(map);
    }

    /**
     * @author:胡立涛
     * @description: TODO 关系：查询概念下知识下的属性值
     * @date: 2024/7/25
     * @param: [targetKnowledgeId]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping("/knowledgepoints/getStepThree/{targetKnowledgeId}")
    List<Map> getStepThree(@PathVariable("targetKnowledgeId") String targetKnowledgeId) {
        return knowledgeDao.getStepThree(targetKnowledgeId);
    }

    /**
     * @author:胡立涛
     * @description: TODO 复杂知识模板：知识集：关系下的知识信息
     * @date: 2024/7/31
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getKnowledgModelStepOne")
    List<Map> getKnowledgModelStepOne(@RequestBody Map map) throws Exception {
        return knowledgeDao.getKnowledgModelStepOne(map);
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据属性code，属性值，知识点查询知识列表
     * @date: 2024/8/1
     * @param: [map] proCode、proValue、knowledgeIds、kpIds
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getKnowledgeByProCode")
    List<Map> getKnowledgeByProCode(@RequestBody Map map) {
        return knowledgeDao.getKnowledgeByProCode(map);
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据知识点，生产国家查询知识数据
     * @date: 2024/8/2
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getRelationKnowledgesByParam")
    List<Map> getRelationKnowledgesByParam(@RequestBody Map map) {
        return knowledgeDao.getRelationKnowledgesByParam(map);
    }


    /**
     * @author:胡立涛
     * @description: TODO 查询知识的影像图片
     * @date: 2024/8/2
     * @param: [knowledgeId]
     * @return: java.util.Map
     */
    @GetMapping("/knowledgepoints/getTifImage/{knowledgeId}")
    Map getTifImage(@PathVariable("knowledgeId") String knowledgeId) {
        return knowledgeDao.getTifImage(knowledgeId);
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据关系属性查询知识
     * @date: 2024/8/5
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getRelationProperty")
    List<Map> getRelationProperty(@RequestBody Map map) {
        return knowledgeDao.getRelationProperty(map);
    }

    /**
     * @author:胡立涛
     * @description: TODO 战斗舰艇判读所需对比图片查询
     * @date: 2024/8/6
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getPics")
    List<Map> getPics(@RequestBody Map map) {
        return knowledgeDao.getPics(map);
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据多个知识点id查询知识点名称
     * @date: 2024/8/7
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getKnowledgePointsMapByIdList")
    List<Map> getKnowledgePointsMapByIdList(@RequestBody Map map) {
        return knowledgeDao.getKnowledgePointsMapByIdList(map);
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据属性名称查询属性code值
     * @date: 2024/8/14
     * @param: [name]
     * @return: java.util.Map
     */
    @GetMapping("/knowledgepoints/getProCode/{name}")
    Map getProCode(@PathVariable("name") String name) {
        return knowledgeDao.getProCode(name);
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据知识点名称查询知识点code
     * @date: 2024/8/14
     * @param: [name]
     * @return: java.util.Map
     */
    @GetMapping("/knowledgepoints/getPointCode/{name}")
    Map getPointCode(@PathVariable("name") String name) {
        return knowledgeDao.getPointCode(name);
    }

    /**
     * @author:胡立涛
     * @description: TODO 查询舰载武器知识点列表
     * @date: 2024/8/15
     * @param: [name]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping("/knowledgepoints/getwqPoint/{name}")
    List<Map> getwqPoint(@PathVariable("name") String name) {
        return knowledgeDao.getwqPoint(name);
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据知识点名称列表查询知识点信息
     * @date: 2024/8/16
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getPoints")
    List<Map> getPoints(@RequestBody Map map) {
        return knowledgeDao.getPoints(map);
    }

    /**
     * @author:胡立涛
     * @description: TODO 舰载武器封面图片获取
     * @date: 2024/8/16
     * @param: [knowledgeId]
     * @return: java.util.Map
     */
    @GetMapping("/knowledgepoints/getPhotoImage/{knowledgeId}")
    Map getPhotoImage(@PathVariable("knowledgeId") String knowledgeId) {
        return knowledgeDao.getPhotoImage(knowledgeId);
    }

    /**
     * @author:胡立涛
     * @description: TODO 查询某知识的所有关系图片
     * @date: 2024/8/19
     * @param: [knowledgeId]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping("/knowledgepoints/getKnowledgePics/{knowledgeId}")
    List<Map> getKnowledgePics(@PathVariable("knowledgeId") String knowledgeId) {
        return knowledgeDao.getKnowledgePics(knowledgeId);
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据typeId查询枚举值列表
     * @date: 2024/8/21
     * @param: [typeId]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping("/knowledgepoints/getmjImage/{typeId}")
    List<Map> getmjImage(@PathVariable("typeId") String typeId) {
        return knowledgeDao.getmjImage(typeId);
    }

    /**
     * @author:胡立涛
     * @description: TODO 舰面标识查询逻辑
     * @date: 2024/8/28
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getxianhao")
    List<Map> getxianhao(@RequestBody Map map) {
        return knowledgeDao.getxianhao(map);
    }

    /**
     * @author:胡立涛
     * @description: TODO deepseek 根据知识名臣查询知识信息
     * @date: 2024/9/5
     * @param: [map]
     * @return: java.util.Map
     */
    @PostMapping(value = "/deepseek/deepseekKnowlege")
    Map deepseekKnowlege(@RequestBody Map map) {
        return knowledgeDao.deepseekKnowlege(map);
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据code查询系统字典
     * @date: 2024/8/29
     * @param: [code]
     * @return: java.util.Map
     */
    @GetMapping("/knowledgepoints/getSysDictionary/{code}")
    Map getSysDictionary(@PathVariable("code") String code) {
        return knowledgeDao.getSysDictionary(code);
    }

    @Autowired
    XxClassDao xxClassDao;


    /**
     * @author:胡立涛
     * @description: TODO 根据类型查询判读要素列表数据
     * @date: 2025/2/17
     * @param: [djData]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "/pd/getysList")
    public ApiResult getysList(@RequestBody DjData djData) {
        try {
            QueryWrapper<DjData> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("type", djData.getType());
            List<DjData> list = djDataDao.selectList(queryWrapper);
            return ApiResultHandler.buildApiResult(200, "操作异常", list);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 智能推荐，将完成数据对接的知识点保存到xx_class表中
     * @date: 2024/9/20
     * @param: [map]
     * @return: void
     */
    @PostMapping(value = "/knowledgepoints/saveClass")
    public void saveClass(@RequestBody Map map) {
        List<String> kpIdList = (List<String>) map.get("list");
        Long userId = Long.valueOf(map.get("userId").toString());
        List<Map> abilityzz = (List<Map>) map.get("abilityzz");
        QueryWrapper<XxClass> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        xxClassDao.delete(queryWrapper);
        for (String kpId : kpIdList) {
            XxClass xxClass = new XxClass();
            xxClass.setClassId(kpId);
            xxClass.setUserId(userId);
            xxClass.setScore("999");
            xxClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
            xxClass.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            xxClassDao.insert(xxClass);
        }
        if (abilityzz != null && abilityzz.size() > 0) {
            for (Map m : abilityzz) {
                String kpId = m.get("kpId").toString();
                String score = m.get("score").toString();
                QueryWrapper<XxClass> query = new QueryWrapper<>();
                query.eq("class_id", kpId);
                XxClass xxClass = new XxClass();
                xxClass.setScore(score);
                xxClass.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                xxClassDao.update(xxClass, query);
            }
        }
    }

    @Autowired
    KnowledgeService knowledgeService;


    /**
     * @author:胡立涛
     * @description: TODO 图片导入知识图谱
     * @date: 2025/1/24
     * @param: [multipartFile]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/pd/importWord")
    public ApiResult importWord(@RequestParam(name = "file") MultipartFile multipartFile) {
        try {
            List<PicImport> picImports = knowledgeService.importWord(multipartFile);
            return ApiResultHandler.buildApiResult(200, "操作成功", picImports);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    @Autowired
    PicImportDao picImportDao;

    /**
     * @author:胡立涛
     * @description: TODO 根据知识ids删除上传的图片信息
     * @date: 2025/2/10
     * @param: [picImport]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/pd/del")
    public ApiResult del(@RequestParam(value = "knowledgeIds", required = true) String knowledgeIds) {
        try {
            String[] knowledgeArr = knowledgeIds.split(",");
            for (int i = 0; i < knowledgeArr.length; i++) {
                QueryWrapper<PicImport> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("knowledge_id", knowledgeArr[i]);
                picImportDao.delete(queryWrapper);
            }

            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    @Autowired
    OntologyIndividualDpDao ontologyIndividualDpDao;
    @Autowired
    OntologyIndividualOpDao ontologyIndividualOpDao;
    @Value(value = "${dj_pic}")
    private String djPic;
    @Value(value = "${dj_localUrlPrefix}")
    private String djLocalUrlPrefix;

    @PostMapping("/dj/testData")
    public ApiResult testData() {
        try {
            // 将结果写入到指定的json文件中
            String filePath = "C:/Users/Administrator/Desktop/单机版数据/export_tips/tips.json";
            File jsonFile = new File(filePath);
            ObjectMapper objectMapper = new ObjectMapper();
            Map map = new HashMap();
            map.put("djId", "单机版数据唯一标识");
            map.put("userId", 1);
            map.put("pdType", 1);
            map.put("pdYs", "");
            map.put("filePath", "F:/0DosMos/bin/exportZipFiles/pic/5a8280c3-c3aa-4c43-a1d8-57f8ed292bd0.png");
            map.put("pdContent", "专家判读建议");
            map.put("score", 1);
            List<Map> rList = new ArrayList<>();
            rList.add(map);
            objectMapper.writeValue(jsonFile, rList);
            // 压缩文件为zip
            String zipFilePath = "C:/Users/Administrator/Desktop/单机版数据/export_tips" + ".zip";
            FileOutputStream fos1 = new FileOutputStream(new File(zipFilePath));
            ZipUtils.toZip("C:/Users/Administrator/Desktop/单机版数据/export_tips", fos1, true);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 单机版：导出知识数据（知识数据、知识与舰载武器关系数据）
     * @date: 2025/2/13
     * @param: [knowledgeIds, kpIds]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/dj/exportKnowledge")
    public ApiResult exportKnowledge(@RequestParam(value = "knowledgeIds", required = true) String knowledgeIds,
                                     @RequestParam(value = "kpIds", required = true) String kpIds) {
        try {
            String basePath = "/dataManage/checkout/movetoFile/";
            // 文件夹名称
            String filePoder = "knowledge_" + UUID.randomUUID().toString().replaceAll("-", "");
            String basefile = basePath + filePoder;
            String forderPath = basePath + filePoder + "/pic";
            Path path = Paths.get(forderPath);
            // 判断文件夹是否存在，如果不存在，创建
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            Map knowledgeMap = new HashMap();
            List<Map> knowledgeList = new ArrayList<>();
            String[] knowledgeArr = knowledgeIds.split(",");
            String[] kpIdArr = kpIds.split(",");
            for (int i = 0; i < knowledgeArr.length; i++) {
                String knowledgeId = knowledgeArr[i];
                String kpId = kpIdArr[i];
                Map parMap = new HashMap();
                parMap.put("kpId", kpId);
                parMap.put("knowledgeId", knowledgeId);
                Map map1 = knowledgeDao.dj_knowledge(parMap);
                Map map = new HashMap();
                map.put("knowledgeId", knowledgeId);
                map.put("knowledgeName", map1.get("knowledge_name").toString());
                map.put("kpId", kpId);
                map.put("kpName", map1.get("kp_name").toString());
                // 查询该知识的结构图
                List<Map> picList = new ArrayList<>();
                parMap = new HashMap();
                parMap.put("knowledgeId", knowledgeId);
                List<Map> list = knowledgeDao.dj_getpicknowledge(parMap);
                if (list != null && list.size() > 0) {
                    for (Map m : list) {
                        String picKnowledgeId = m.get("individual_id").toString();
                        // 图片名称
                        QueryWrapper<OntologyIndividualDp> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("data_property_id", "d9ad382d-457f-4b6e-ab71-bb875e51da15");
                        queryWrapper.eq("individual_id", picKnowledgeId);
                        OntologyIndividualDp ontologyIndividualDp = ontologyIndividualDpDao.selectOne(queryWrapper);
                        Map picMap = new HashMap();
                        picMap.put("picKnowledgeId", picKnowledgeId);
                        picMap.put("picName", ontologyIndividualDp);
                        // 图片地址
                        queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("individual_id", picKnowledgeId);
                        queryWrapper.eq("data_property_id", "f5c1868b-a403-47cd-9f62-296e61e9925a");
                        List<OntologyIndividualDp> ontologyIndividualDps = ontologyIndividualDpDao.selectList(queryWrapper);
                        List<OntologyIndividualDp> newList = new ArrayList<>();
                        // 将图片复制到指定位置
                        if (ontologyIndividualDps != null && ontologyIndividualDps.size() > 0) {
                            for (OntologyIndividualDp bean : ontologyIndividualDps) {
                                try {
                                    String picValue = bean.getDataPropertyValue();
                                    copyPic(picValue, forderPath);
                                    newList.add(bean);
                                } catch (Exception e) {
                                    System.out.println("------复制图片异常----");
                                    e.printStackTrace();
                                    continue;
                                }
                            }
                        }
                        picMap.put("picPath", newList);
                        // 拍摄位置
                        queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("individual_id", picKnowledgeId);
                        queryWrapper.eq("data_property_id", "569900a5-ab33-4756-b9c3-a2673ddc30b5");
                        ontologyIndividualDp = ontologyIndividualDpDao.selectOne(queryWrapper);
                        picMap.put("pswz", ontologyIndividualDp);
                        // 拍摄角度
                        queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("individual_id", picKnowledgeId);
                        queryWrapper.eq("data_property_id", "21be52a5-b967-4fa8-8355-c70fadd5bc3d");
                        ontologyIndividualDp = ontologyIndividualDpDao.selectOne(queryWrapper);
                        picMap.put("psjd", ontologyIndividualDp);
                        picList.add(picMap);
                    }
                }
                // 查询知识的舰载武器
                QueryWrapper<OntologyIndividualOp> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("object_property_id", "2812010b-81a3-484a-80bc-83d4866e6dd1");
                queryWrapper.eq("domain_individual_id", knowledgeId);
                List<OntologyIndividualOp> ontologyIndividualOps = ontologyIndividualOpDao.selectList(queryWrapper);
                map.put("jzwqList", ontologyIndividualOps);
                map.put("picList", picList);
                knowledgeList.add(map);
            }
            // 将数据写到指定的json文件中
            String filePath = basePath + filePoder + "/knowledge.json";
            File jsonFile = new File(filePath);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(jsonFile, knowledgeList);
            // 生成zip文件
            // 输出位置
            String zipFilePath = basePath + filePoder + ".zip";
            FileOutputStream fos1 = new FileOutputStream(new File(zipFilePath));
            ZipUtils.toZip(basePath + filePoder, fos1, true);
            // 删除非zip文件
            // 删除文件夹及其所有子文件夹和文件
            Files.walk(Paths.get(basePath + filePoder))
                    .sorted(java.util.Comparator.reverseOrder()) // 先删除子文件夹，后删除父文件夹
                    .forEach(path1 -> {
                        try {
                            Files.delete(path1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Deleted: " + path1);
                    });
            return ApiResultHandler.buildApiResult(200, "操作成功", djLocalUrlPrefix + filePoder + ".zip");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    @Autowired
    DjDataDao djDataDao;
    @Autowired
    OntologyDataPropertyDao ontologyDataPropertyDao;
    @Autowired
    SysDictionaryDao sysDictionaryDao;
    @Autowired
    OntologyIndividualDao ontologyIndividualDao;
    @Autowired
    DjKpDao djKpDao;

    /**
     * @author:胡立涛
     * @description: TODO 单机版：导出基本信息（知识点，判读要素、判读要素内容、所有舰载武器）
     * @date: 2025/2/14
     * @param: []
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/dj/exportBaseInfo")
    public ApiResult exportBaseInfo() {
        try {
            String basePath = "/dataManage/checkout/movetoFile/";
            // 文件夹名称
            String filePoder = "base_" + UUID.randomUUID().toString().replaceAll("-", "");
            String basefile = basePath + filePoder;
            String forderPath = basePath + filePoder + "/pic";
            Path path = Paths.get(forderPath);
            // 判断文件夹是否存在，如果不存在，创建
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            Map rMap = new HashMap();
            // 查询知识点数据
            List<Map> kpList = knowledgeDao.getTree();
            // 查询要素信息
            QueryWrapper<DjData> queryWrapper = new QueryWrapper();
            List<DjData> djList = djDataDao.selectList(queryWrapper);
            for (DjData djData : djList) {
                // 枚举
                if (!djData.getWbType().equals("1")) {
                    String tpName = djData.getTpMb();
                    QueryWrapper<OntologyDataProperty> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.eq("name", tpName.trim());
                    System.out.println("-------name名字为：" + tpName);
                    OntologyDataProperty ontologyDataProperty = ontologyDataPropertyDao.selectOne(queryWrapper1);
                    String typeId = ontologyDataProperty.getValue().trim();
                    QueryWrapper<SysDictionary> queryWrapper2 = new QueryWrapper<>();
                    queryWrapper2.eq("type_id", typeId);
                    List<SysDictionary> sysDictionaries = sysDictionaryDao.selectList(queryWrapper2);
                    List<Map> dataList = new ArrayList<>();
                    for (SysDictionary sysDictionary : sysDictionaries) {
                        Map map = new HashMap();
                        map.put("code", sysDictionary.getCode());
                        map.put("name", sysDictionary.getName());
                        map.put("pics", sysDictionary.getDescription());
                        dataList.add(map);
                        String des = sysDictionary.getDescription() == null ? "" : sysDictionary.getDescription();
                        if (des != "") {
                            String[] picArr = des.split(",");
                            for (int i = 0; i < picArr.length; i++) {
                                // 将图片复制到指定位置
                                try {
                                    // 2024/2024-09-12/0cd114e1-34a7-422d-bd0f-353d00f9e4e8.jpg
                                    String picValue = picArr[i];
                                    copyPic(picValue, forderPath);
                                } catch (Exception e) {
                                    System.out.println("------复制图片异常----");
                                    continue;
                                }
                            }
                        }
                    }
                    djData.setDataList(dataList);
                }
            }
            // 舰载武器知识点
            List<Map> list = knowledgeDao.getwqPoint("舰载武器");
            List<Map> wqKpList = new ArrayList<>();
            List<String> l = new ArrayList<>();
            for (Map map : list) {
                Map map1 = new HashMap();
                map1.put("kpId", map.get("id"));
                map1.put("kpName", map.get("name"));
                wqKpList.add(map1);
                l.add(map.get("id").toString());
            }
            // 舰载武器国家
            List<String> countryList = new ArrayList<>();
            String countries = "意大利,英国,美国,俄罗斯,韩国,法国,日本,荷兰";
            // 舰载武器数据
            List<Map> wqDataList = new ArrayList<>();
            QueryWrapper<OntologyIndividual> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.in("class_id", l);
            List<OntologyIndividual> ontologyIndividuals = ontologyIndividualDao.selectList(queryWrapper1);
            if (ontologyIndividuals != null && ontologyIndividuals.size() > 0) {
                for (OntologyIndividual ontologyIndividual : ontologyIndividuals) {
                    Map wuMap = new HashMap();
                    String knowledgeId = ontologyIndividual.getId();
                    String knowledgeName = ontologyIndividual.getName();
                    wuMap.put("knowledgeId", knowledgeId);
                    wuMap.put("knowledgeName", knowledgeName);
                    // 舰载武器国籍
                    QueryWrapper<OntologyIndividualDp> queryWrapper2 = new QueryWrapper<>();
                    queryWrapper2.eq("individual_id", knowledgeId);
                    queryWrapper2.eq("data_property_id", "ee616c5e-a085-48e1-932a-02dba7952f86");
                    OntologyIndividualDp ontologyIndividualDp = ontologyIndividualDpDao.selectOne(queryWrapper2);
                    wuMap.put("country", ontologyIndividualDp.getDataPropertyValue());
                    // 舰载武器封面图
                    queryWrapper2 = new QueryWrapper<>();
                    queryWrapper2.eq("individual_id", knowledgeId);
                    queryWrapper2.eq("data_property_id", "bf73f658-bbef-4ab4-b6c4-8bbb6aa2aa14");
                    List<OntologyIndividualDp> ontologyIndividualDps1 = ontologyIndividualDpDao.selectList(queryWrapper2);
                    String photo = null;
                    if (ontologyIndividualDps1 != null && ontologyIndividualDps1.size() > 0) {
                        ontologyIndividualDp = ontologyIndividualDps1.get(0);
                        photo = ontologyIndividualDp == null ? null : ontologyIndividualDp.getDataPropertyValue();
                    }
                    wuMap.put("photoImage", photo);
                    // 将图片复制到指定位置
                    try {
                        // 2024/2024-09-12/0cd114e1-34a7-422d-bd0f-353d00f9e4e8.jpg
                        String picValue = ontologyIndividualDp.getDataPropertyValue();
                        copyPic(picValue, forderPath);
                    } catch (Exception e) {
                        System.out.println("------复制图片异常----");
                    }
                    // 舰载武器影像封面图
                    queryWrapper2 = new QueryWrapper<>();
                    queryWrapper2.eq("individual_id", knowledgeId);
                    queryWrapper2.eq("data_property_id", "2b106ec3-efcf-4444-bf9d-f1307a2ded6e");
                    List<OntologyIndividualDp> ontologyIndividualDps = ontologyIndividualDpDao.selectList(queryWrapper2);
                    String tifPhoto = null;
                    if (ontologyIndividualDps != null && ontologyIndividualDps.size() > 0) {
                        ontologyIndividualDp = ontologyIndividualDps.get(0);
                        tifPhoto = ontologyIndividualDp == null ? null : ontologyIndividualDp.getDataPropertyValue();
                    }
                    wuMap.put("tifImage", tifPhoto);
                    // 将图片复制到指定位置
                    try {
                        // 2024/2024-09-12/0cd114e1-34a7-422d-bd0f-353d00f9e4e8.jpg
                        String picValue = ontologyIndividualDp.getDataPropertyValue();
                        copyPic(picValue, forderPath);
                    } catch (Exception e) {
                        System.out.println("------复制舰载武器影像封面图片异常----");
                    }
                    wqDataList.add(wuMap);
                }
            }
            // 知识点树形菜单
            rMap.put("kpList", kpList);
            // 判读要素及要素数据
            rMap.put("ysList", djList);
            // 舰载武器知识点
            rMap.put("wqKpList", wqKpList);
            // 舰载武器国家
            rMap.put("countries", countries);
            // 舰载武器数据
            rMap.put("wqDataList", wqDataList);
            QueryWrapper<DjKp> queryWrapper2 = new QueryWrapper<>();
            List<DjKp> djKps = djKpDao.selectList(queryWrapper2);
            // 知识点与六大类关系
            rMap.put("djKpList", djKps);
            // 将结果json写入指定位置
            String filePath = basePath + filePoder + "/base.json";
            File jsonFile = new File(filePath);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(jsonFile, rMap);
            // 生成zip文件
            // 输出位置
            String zipFilePath = basePath + filePoder + ".zip";
            FileOutputStream fos1 = new FileOutputStream(new File(zipFilePath));
            ZipUtils.toZip(basePath + filePoder, fos1, true);
            // 删除非zip文件
            // 删除文件夹及其所有子文件夹和文件
            Files.walk(Paths.get(basePath + filePoder))
                    .sorted(java.util.Comparator.reverseOrder()) // 先删除子文件夹，后删除父文件夹
                    .forEach(path1 -> {
                        try {
                            Files.delete(path1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Deleted: " + path1);
                    });
            return ApiResultHandler.buildApiResult(200, "操作成功", djLocalUrlPrefix + filePoder + ".zip");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    public void copyPic(String path, String forderPath) throws Exception {
        // 2024/2024-09-12/0cd114e1-34a7-422d-bd0f-353d00f9e4e8.jpg
        String picValue = path;
        // 图片名称
        String picName = picValue.substring(picValue.lastIndexOf("/") + 1);
        // 图谱中图片的完整路径
        String oldPath = djPic + picValue;
        File file = new File(oldPath);
        FileInputStream inputStream = new FileInputStream(file);
        // 新图片地址
        File file1 = new File(forderPath + "/" + picName);
        FileOutputStream outputStream = new FileOutputStream(file1);
        byte[] b = new byte[1024];
        while (inputStream.read(b) != -1) {
            outputStream.write(b);
        }
        outputStream.close();
        inputStream.close();
    }

    @Autowired
    PdPropertyDao pdPropertyDao;


    /**
     * @author:胡立涛
     * @description: TODO 图谱属性管理中：属性添加、修改、删除操作调用接口
     * @date: 2025/4/23
     * @param: [ontologyDataProperty]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/pd/proceInfo")
    public ApiResult saveInfo(@RequestBody OntologyDataProperty ontologyDataProperty) {
        try {
            if (ontologyDataProperty.getDelFlg() != null && ontologyDataProperty.getDelFlg() == 1) {
                // 删除pdProperty表中的数据
                QueryWrapper<PdProperty> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("data_property_id", ontologyDataProperty.getId());
                pdPropertyDao.delete(queryWrapper1);
            }
            // 根据名字查询属性信息
            QueryWrapper<OntologyDataProperty> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name", ontologyDataProperty.getName());
            OntologyDataProperty detail = ontologyDataPropertyDao.selectOne(queryWrapper);
            if (detail != null) {
                // 更新图谱表数据
                OntologyDataProperty updateBean = new OntologyDataProperty();
                updateBean.setSuperPropertyId(ontologyDataProperty.getSuperPropertyId());
                updateBean.setUri(ontologyDataProperty.getUri());
                updateBean.setCode(ontologyDataProperty.getCode());
                updateBean.setId(detail.getId());
                ontologyDataPropertyDao.updateById(updateBean);
                // 删除pdProperty表中的数据
                QueryWrapper<PdProperty> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("data_property_id", detail.getId());
                pdPropertyDao.delete(queryWrapper1);
                // 向pdProperty表添加数据
                String uri = ontologyDataProperty.getUri();
                if (uri != null && uri.length() > 0) {
                    String groupName = ontologyDataProperty.getSuperPropertyId() == null ? "" : ontologyDataProperty.getSuperPropertyId();
                    Integer orderNum = ontologyDataProperty.getCode() == null ? null : Integer.parseInt(ontologyDataProperty.getCode());
                    String dataPropertyId = detail.getId();
                    String typeId = detail.getValue() == null ? "" : detail.getValue();
                    String propertyName = detail.getName();
                    String[] uriArr = uri.split(",");
                    for (int i = 0; i < uriArr.length; i++) {
                        PdProperty bean = new PdProperty();
                        bean.setDataPropertyId(dataPropertyId);
                        bean.setPropertyName(propertyName);
                        bean.setLabelType(Integer.parseInt(uriArr[i]));
                        bean.setDataType(detail.getDataType());
                        bean.setGroupName(groupName);
                        bean.setOrderNum(orderNum);
                        bean.setUnitName(detail.getDataUnit());
                        bean.setTypeId(typeId);
                        pdPropertyDao.insert(bean);
                    }
                }
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据判读类型（6大类）查询判读标签
     * @date: 2025/4/23
     * @param: [pdProperty]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/pd/labelList")
    public ApiResult labelList(@RequestBody PdProperty pdProperty) {
        try {
            QueryWrapper<PdProperty> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("label_type", pdProperty.getLabelType());
            queryWrapper.orderByAsc("group_name,order_num");
            List<PdProperty> pdProperties = pdPropertyDao.selectList(queryWrapper);
            List<PdProperty> rList = new ArrayList<>();
            if (pdProperties != null && pdProperties.size() > 0) {
                for (PdProperty bean : pdProperties) {
                    // 根据id查询属性信息
                    QueryWrapper<OntologyDataProperty> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.eq("id", bean.getDataPropertyId());
                    OntologyDataProperty ontologyDataProperty = ontologyDataPropertyDao.selectOne(queryWrapper1);
                    String dataType = ontologyDataProperty == null ? "" : ontologyDataProperty.getDataType();
                    bean.setPropertyName(ontologyDataProperty.getName());
                    if (dataType != "" || !dataType.equals("")) {
                        // 枚举类型
                        if (dataType.equals("ENUM")) {
                            QueryWrapper<SysDictionary> sysDictionaryQueryWrapper = new QueryWrapper<>();
                            sysDictionaryQueryWrapper.eq("type_id", bean.getTypeId());
                            List<SysDictionary> sysDictionaries = sysDictionaryDao.selectList(sysDictionaryQueryWrapper);
                            bean.setSysDictionaries(sysDictionaries);
                        }
                    }
                    rList.add(pdProperty);
                }
            }
            Map rMap = new HashMap();
            rMap.put("rList", rList);
            rMap.put("tupuPicServer", tupuPicServer);
            return ApiResultHandler.buildApiResult(200, "操作成功", pdProperties);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    @Autowired
    PdOrderDao pdOrderDao;

    /**
     * @author:胡立涛
     * @description: TODO 增加判读排序信息
     * @date: 2025/4/24
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/pdorder/addInfo")
    public ApiResult addInfo(@RequestBody Map map) {
        try {
            int labelType = Integer.parseInt(map.get("labelType").toString());
            int userId = Integer.parseInt(map.get("userId").toString());
            String userName = map.get("userName").toString();
            List<Map> mapList = (List<Map>) map.get("list");
            for (Map m : mapList) {
                String rowName = m.get("rowName").toString();
                int useCount = Integer.parseInt(m.get("useCount").toString());
                String groupName = m.get("groupName") == null ? "" : m.get("groupName").toString();
                // 根据rowName,userId,labelType查询数据
                QueryWrapper<PdOrder> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("user_id", userId);
                queryWrapper.eq("row_name", rowName);
                queryWrapper.eq("label_type", labelType);
                PdOrder bean = pdOrderDao.selectOne(queryWrapper);
                if (bean == null) {
                    PdOrder pdOrder = new PdOrder();
                    pdOrder.setUserId(userId);
                    pdOrder.setUserName(userName);
                    pdOrder.setLabelType(labelType);
                    pdOrder.setRowName(rowName);
                    pdOrder.setUseCount(useCount);
                    pdOrder.setGroupName(groupName);
                    pdOrder.setOrderNum(Integer.parseInt(m.get("orderNum").toString()));
                    pdOrder.setDataPropertyId(m.get("dataPropertyId").toString());
                    pdOrder.setCreateTime(new Timestamp(System.currentTimeMillis()));
                    pdOrderDao.insert(pdOrder);
                } else {
                    bean.setGroupName(groupName);
                    bean.setOrderNum(Integer.parseInt(m.get("orderNum").toString()));
                    bean.setUseCount(bean.getUseCount() + useCount);
                    bean.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                    pdOrderDao.updateById(bean);
                }
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
     * @description: TODO 根据用户id，判读标签类型，查询判读排序列表
     * @date: 2025/4/24
     * @param: [pdOrder]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/pdorder/getList")
    public ApiResult getList(@RequestBody PdOrder pdOrder) {
        try {
            QueryWrapper<PdOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", pdOrder.getUserId());
            queryWrapper.eq("label_type", pdOrder.getLabelType());
            queryWrapper.orderByDesc("use_count").orderByDesc("group_name").orderByAsc("order_num");
            List<PdOrder> pdOrders = pdOrderDao.selectList(queryWrapper);
            List<PdOrder> rList = new ArrayList<>();
            if (pdOrders != null && pdOrders.size() > 0) {
                for (PdOrder bean : pdOrders) {
                    // 根据id查询属性信息
                    QueryWrapper<OntologyDataProperty> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.eq("id", bean.getDataPropertyId());
                    OntologyDataProperty ontologyDataProperty = ontologyDataPropertyDao.selectOne(queryWrapper1);
                    String dataType = ontologyDataProperty == null ? "" : ontologyDataProperty.getDataType();
                    bean.setDataType(dataType);
                    bean.setUnitName(ontologyDataProperty.getDataUnit() == null ? "" : ontologyDataProperty.getDataUnit());
                    bean.setRowName(ontologyDataProperty.getName());
                    if (dataType != "" || !dataType.equals("")) {
                        // 枚举类型
                        if (dataType.equals("ENUM")) {
                            QueryWrapper<SysDictionary> sysDictionaryQueryWrapper = new QueryWrapper<>();
                            sysDictionaryQueryWrapper.eq("type_id", ontologyDataProperty.getValue());
                            List<SysDictionary> sysDictionaries = sysDictionaryDao.selectList(sysDictionaryQueryWrapper);
                            bean.setSysDictionaries(sysDictionaries);
                        }
                    }
                    rList.add(bean);
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
     * @description: TODO 图谱增加判读标签，调用该接口，根据用户id，标签类型删除个人热度标签信息
     * @date: 2025/6/9
     * @param: [pdOrder]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/pdorder/delPdOrder")
    public ApiResult delPdOrder(@RequestBody PdOrder pdOrder) {
        try {
            if (pdOrder.getLabelTypes() != null) {
                String[] typsArr = pdOrder.getLabelTypes().split(",");
                for (int i = 0; i < typsArr.length; i++) {
                    QueryWrapper<PdOrder> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("label_type", Integer.parseInt(typsArr[i]));
                    pdOrderDao.delete(queryWrapper);
                }
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    @Autowired
    CountryOrderDao countryOrderDao;

    /**
     * @author:胡立涛
     * @description: TODO 舰船和飞机识别国籍排序
     * @date: 2025/6/9
     * @param: [countryOrder]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/countryOrder/addInfo")
    public ApiResult addInfo(@RequestBody CountryOrder countryOrder) {
        try {
            if (countryOrder.getCountryNames() != null) {
                // 删除该用户国籍排序
                QueryWrapper<CountryOrder> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("user_id", countryOrder.getUserId());
                queryWrapper.eq("flg", countryOrder.getFlg());
                countryOrderDao.delete(queryWrapper);
                String[] countryNameArr = countryOrder.getCountryNames().split(",");
                for (int i = 0; i < countryNameArr.length; i++) {
                    CountryOrder bean = new CountryOrder();
                    bean.setCountryName(countryNameArr[i]);
                    bean.setUserId(countryOrder.getUserId());
                    bean.setFlg(countryOrder.getFlg());
                    bean.setOrderNum(i + 1);
                    countryOrderDao.insert(bean);
                }
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

}
