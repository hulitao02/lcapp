package com.cloud.backend.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloud.backend.dao.KnowledgePointsDao;
import com.cloud.backend.service.KnowledgePointsService;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.feign.exam.ExamFeign;
import com.cloud.feign.knowledge.KnowledgeFeign;
import com.cloud.model.common.KnowledgePoints;
import com.cloud.model.common.KnowledgePointsBean;
import com.cloud.model.common.Page;
import com.cloud.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@RestController
public class KnowledgePointController {

    @Resource
    private KnowledgePointsService knowledgePointsService;
    @Autowired
    private KnowledgePointsDao knowledgePointsDao;
    @Autowired
    private ExamFeign examFeign;
    // 根节点标示字符
    @Value(value = "${root-label}")
    private String rootLabel;
    // 数据库标识 1：达梦数据库 2：pg数据库
    @Value(value = "${db-type}")
    private Integer dbType;


    /**
     * 删除权限标识
     *
     * @param id
     */
    //@LogAnnotation(module = LogModule.DELETE_PERMISSION)
    @PreAuthorize("hasAuthority('back:knowledgepoints:delete')")
    @DeleteMapping("/knowledgepoints/{id}")
    public void delete(@PathVariable Long id) {
        knowledgePointsService.delete(id);
    }


    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @PreAuthorize("hasAuthority('back:knowledgepoints:query')")
    @GetMapping("/knowledgepoints")
    public Page<KnowledgePoints> findPermissions(@RequestParam Map<String, Object> params) {
        return knowledgePointsService.findByPage(params);
    }


    /**
     * 菜单table
     *
     * @param parentId
     * @param all
     * @param list
     */
    private void setSortTable(String parentId, List<KnowledgePointsBean> all, List<KnowledgePointsBean> list) {
        all.forEach(a -> {
            list.add(a);
            setSortTable(a.getId(), all, list);
        });
    }

    @Autowired
    KnowledgeFeign knowledgeFeign;

    /**
     * 查询所有菜单
     */
    //@PreAuthorize("hasAuthority('back:knowledgepoints:query')")
    @GetMapping("/knowledgepoints/all")
    public ApiResult findAll(@RequestParam(required = false) Long examId, @RequestParam(required = false) Long departId, @RequestParam(required = false) Integer type) throws Exception {
        try {
            List<Map> oldList = new ArrayList<>();
            List<Map> aClass = knowledgeFeign.getKnowledgePointListFeign();
            for (Map map : aClass) {
                Map bean = new HashMap();
                bean.put("id", map.get("id").toString());
                bean.put("pointname", map.get("pointname").toString());
                bean.put("parentid", map.get("parentid").toString());
                oldList.add(bean);
            }
            JSONArray result = listToTree(JSONArray.parseArray(JSON.toJSONString(oldList)), "id", "parentid", "children");
            return ApiResultHandler.buildApiResult(200, "操作成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }

    }

    @GetMapping("/knowledgepoints/alltwo")
    public List<Map> alltwo(@RequestParam(required = false) Long examId, @RequestParam(required = false) Long departId, @RequestParam(required = false) Integer type) throws Exception {
        try {
            List<Map> oldList = new ArrayList<>();
            List<Map> aClass = knowledgeFeign.getKnowledgePointListFeign();
            for (Map map : aClass) {
                Map bean = new HashMap();
                bean.put("id", map.get("id").toString());
                bean.put("pointName", map.get("pointname").toString());
                bean.put("parentId", map.get("parentid").toString());
                oldList.add(bean);
            }
            return oldList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


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
     * 知识点树
     *
     * @param parentId
     * @param all
     * @param list
     */
    private void setMenuTree(Long parentId, List<KnowledgePoints> all, List<KnowledgePoints> list) {
        all.forEach(kp -> {
            if (parentId.longValue() == kp.getParentId().longValue()) {
                list.add(kp);
                List<KnowledgePoints> child = new ArrayList<>();
                kp.setChild(child);
                setMenuTree(kp.getId(), all, child);
            }
        });
    }


    /*@PreAuthorize("hasAnyAuthority('back:menu:set2role','back:menu:query')")*/
    @GetMapping("/knowledgepoints/tree")
    public List<KnowledgePoints> findMenuTree() {
        List<KnowledgePoints> all = knowledgePointsService.findAll();
        List<KnowledgePoints> list = new ArrayList<>();
        setMenuTree(0L, all, list);
        return list;
    }

    @GetMapping("/getKnowledgePointsById/{id}")
    public KnowledgePoints getKnowledgePointsById(@PathVariable Long id) {
        return knowledgePointsService.findById(id);
    }

    @PostMapping("/getKnowledgePointsMapByIdList")
    public Map<Long, String> getKnowledgePointsMapByIdList(@RequestBody List<Long> idList) {
        if (CollectionUtils.isNotEmpty(idList)) {
            List<KnowledgePoints> list = knowledgePointsService.lambdaQuery()
                    .select(KnowledgePoints::getId, KnowledgePoints::getPointName)
                    .in(KnowledgePoints::getId, idList).list();
            if (CollectionUtils.isNotEmpty(list)) {
                Map<Long, String> map = list.stream()
                        .collect(Collectors.toMap(KnowledgePoints::getId, KnowledgePoints::getPointName));
                return map;
            }
        }
        return Collections.emptyMap();
    }

    @GetMapping("/getKnowledgePointNameMap")
    public Map<String, Long> getKnowledgePointNameMap() {
        return knowledgePointsService.getKnowledgePointNameMap();
    }

    @GetMapping("/getKnowledgePointIdMap")
    public Map<Long, String> getKnowledgePointIdMap() {
        return knowledgePointsService.getKnowledgePointIdMap();
    }

    /**
     * @author:胡立涛
     * @description: TODO 知识数据同步：添加知识点
     * @date: 2021/12/8
     * @param: [knowledgePoints]
     * @return: java.lang.String
     */
    @PostMapping("/knowledgepoints/saveInfo")
    public String saveInfo(KnowledgePoints knowledgePoints) {
        try {
            // 知识点名称已存在
            if (knowledgePointsService.findByName(knowledgePoints.getPointName()) != null) {
                return "100";
            }
            knowledgePointsDao.saveInfo(knowledgePoints);
            return "200";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 知识数据同步：更新知识点名称
     * @date: 2021/12/8
     * @param: [map]
     * @return: java.lang.String
     */
    @PostMapping(value = "/knowledgepoints/updateClass")
    public String updateClass(KnowledgePoints knowledgePoints) {
        try {
            Map knowledgePointsByCode = knowledgePointsDao.getKnowledgePointsByCode(knowledgePoints.getCode());
            KnowledgePoints bean = knowledgePointsService.findById(Long.valueOf(knowledgePointsByCode.get("id").toString()));
            bean.setPointName(knowledgePoints.getPointName());
            knowledgePointsService.update(bean);
            return "200";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 知识图谱数据同步：更新parent_id的值
     * @date: 2021/12/8
     * @param: [map]
     * @return: java.lang.String
     */
    @PostMapping(value = "/knowledgepoints/updateParentId")
    public void updateParentId(KnowledgePoints knowledgePoints) {
        try {
            Map knowledgeMap = knowledgePointsDao.getKnowledgePointsByCode(knowledgePoints.getParentCode());
            knowledgePoints.setParentId(Long.valueOf(knowledgeMap.get("id").toString()));
            knowledgePointsService.update(knowledgePoints);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 知识数据同步：根据parentId查询知识点
     * @date: 2021/12/8
     * @param: [knowledgePoints]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getClass")
    public List<Map> getClass(KnowledgePoints knowledgePoints) throws Exception {
        if (dbType == 1) {
            return knowledgePointsDao.getClassDM(knowledgePoints.getParentId());
        }
        return knowledgePointsDao.getClass(knowledgePoints.getParentId());
    }

    /**
     * @author:胡立涛
     * @description: TODO 知识数据同步：根据id查询知识点
     * @date: 2021/12/8
     * @param: [knowledgePoints]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getClassById")
    public List<Map> getClassById(@RequestParam KnowledgePoints knowledgePoints) throws Exception {
        if (knowledgePoints.getId() != null) {
            if (dbType == 1) {
                return knowledgePointsDao.getClassByIdDM(knowledgePoints.getId());
            }
            return knowledgePointsDao.getClassById(knowledgePoints.getId());
        }
        return null;
    }


    /**
     * @author:胡立涛
     * @description: TODO 知识数据同步：根据id查询知识点
     * @date: 2021/12/8
     * @param: [knowledgePoints]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping(value = "/knowledgepoints/getClassByIdGet/{rootId}")
    public List<Map> getClassByIdGet(@PathVariable("rootId") Long rootId) throws Exception {
        KnowledgePoints knowledgePoints = new KnowledgePoints();
        knowledgePoints.setId(rootId);
        if (knowledgePoints.getId() != null) {
            if (dbType == 1) {
                return knowledgePointsDao.getClassByIdDM(knowledgePoints.getId());
            }
            return knowledgePointsDao.getClassById(knowledgePoints.getId());
        }
        return null;
    }

    /**
     * @author:胡立涛
     * @description: TODO 知识数据同步： 删除知识点
     * @date: 2021/12/9
     * @param: [knowledgePoints]
     * @return: java.lang.String
     */
    @PostMapping(value = "/knowledgepoints/delClass")
    public String delClass(KnowledgePoints knowledgePoints) {
        try {
            knowledgePointsService.delClass(knowledgePoints);
            return "200";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据知识code查询知识信息
     * @date: 2022/1/25
     * @param: [code]
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     */
    @GetMapping("/getKnowledgePointsByCode/{code}")
    public Map<String, Object> getKnowledgePointsByCode(@PathVariable("code") String code) {
        return knowledgePointsDao.getKnowledgePointsByCode(code);
    }


    /**
     * @author:胡立涛
     * @description: TODO 批量 知识点数据同步
     * @date: 2022/2/18
     * @param: [list]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "/bathPoint")
    public ApiResult bathPoint(@RequestBody List<Map> list) {
        try {
            if (list == null || list.size() == 0) {
                return ApiResultHandler.buildApiResult(100, "参数为空", null);
            }
            knowledgePointsService.bathPoint(list);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 获取绑定数据的知识点树
     * @date: 2022/3/14
     * @param: [pointIds]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping(value = "/knowledgepoints/getKnowledgeBind/{ids}")
    public List<Map> getKnowledgeBind(@PathVariable("ids") String ids) {
        String[] idsarr = ids.split(",");
        List<Long> idsList = new ArrayList<>();
        for (int i = 0; i < idsarr.length; i++) {
            idsList.add(Long.valueOf(idsarr[i]));
        }
        if (dbType == 1) {
            return knowledgePointsDao.getKnowledgeBindDM(idsList);
        }
        return knowledgePointsDao.getKnowledgeBind(idsList);
    }

    /**
     * @author:胡立涛
     * @description: TODO 获取根节点信息
     * @date: 2022/4/13
     * @param: []
     * @return: java.util.Map
     */
    @GetMapping(value = "/knowledgepoints/getRootPoint")
    public Map getRootPoint() {
        return knowledgePointsDao.getRootPoint(rootLabel);
    }

    /**
     * @author:胡立涛
     * @description: TODO 获取根节点信息 返回值小写处理
     * @date: 2022/10/25
     * @param: []
     * @return: java.util.Map
     */
    @GetMapping(value = "/knowledgepoints/getRootPointInfo")
    public ApiResult getRootPointInfo() {
        try {
            Map rootPoint = knowledgePointsDao.getRootPoint(rootLabel);
            return ApiResultHandler.buildApiResult(200, "操作成功", rootPoint);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author: 胡立涛
     * @description: TODO 根据知识点id删除knowledgePoint信息
     * @date: 2022/5/28
     * @param: [kpIds]
     * @return: void
     */
    @PostMapping(value = "/knowledgepoints/delKnowledgePointManage")
    public void delKnowledgePointManage(@RequestParam Map<String, Object> map) {
        String kpCodeStr = map.get("kpCodeStr").toString();
        kpCodeStr = kpCodeStr.substring(0, kpCodeStr.length() - 1);
        String[] kpCodeArr = kpCodeStr.split(",");
        Long[] kpIds = new Long[kpCodeArr.length];
        for (int i = 0; i < kpCodeArr.length; i++) {
            String kpCode = kpCodeArr[i];
            // 根据知识点code查询知识点id
            Map knowledgePointsByCode = knowledgePointsDao.getKnowledgePointsByCode(kpCode);
            if (knowledgePointsByCode == null) {
                kpIds[i] = -1L;
            } else {
                long kpId = Long.valueOf(knowledgePointsByCode.get("id").toString());
                kpIds[i] = kpId;
            }
        }
        knowledgePointsDao.delKnowledgePointManage(kpIds);
    }

    @PostMapping("/getKnowledgePointIdMapByIds")
    public Map<Long, String> getKnowledgePointIdMapByIds(@RequestBody Collection<Long> ids) {
        return knowledgePointsService.getKnowledgePointIdMapByIds(ids);
    }

}
