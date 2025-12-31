package com.cloud.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.backend.dao.KnowledgePointsDao;
import com.cloud.backend.service.KnowledgePointsService;
import com.cloud.model.common.KnowledgePoints;
import com.cloud.model.common.Page;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.utils.PageUtil;
import com.cloud.utils.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgePointsServiceImpl extends ServiceImpl<KnowledgePointsDao, KnowledgePoints> implements KnowledgePointsService {

    // 数据库标识 1：达梦数据库 2：pg数据库
    @Value(value = "${db-type}")
    private Integer dbType;
    @Resource
    private KnowledgePointsDao knowledgePointsDao;
    // 根节点标示字符
    @Value(value = "${root-label}")
    private String rootLabel;

    @Override
    public int saveKnowledgePoints(KnowledgePoints knowledgePoints) {
        if (AppUserUtil.getLoginAppUser() == null) {
            knowledgePoints.setCreator(0L);
        } else {
            knowledgePoints.setCreator(AppUserUtil.getLoginAppUser().getId());
        }
        knowledgePoints.setCreateTime(new Date());
        knowledgePoints.setUpdateTime(new Date());
        knowledgePoints.setLevel(0);
        knowledgePoints.setStatus(1);
        if (knowledgePoints.getParentId() == null) {
            knowledgePoints.setParentId(0L);
        }
        return knowledgePointsDao.save(knowledgePoints);
    }

    @Override
    public int update(KnowledgePoints knowledgePoints) {
        return knowledgePointsDao.update(knowledgePoints);
    }

    @Override
    public int delete(Long id) {
        List<Long> parentIdList = knowledgePointsDao.parentIdList();
        if (parentIdList.contains(id)) {
            throw new IllegalArgumentException("存在子节点，无法删除！");
        }
        return knowledgePointsDao.delete(id);
    }

    @Override
    public KnowledgePoints findById(Long id) {
        return knowledgePointsDao.findById(id);
    }

    @Override
    public KnowledgePoints findByName(String name) {
        return knowledgePointsDao.findByName(name);
    }

    @Override
    public Page<KnowledgePoints> findByPage(Map<String, Object> params) {
        int total = knowledgePointsDao.count(params);
        List<KnowledgePoints> list = Collections.emptyList();
        if (total > 0) {
            PageUtil.pageParamConver(params, true);
            list = knowledgePointsDao.findData(params);
        }
        return new Page<>(total, list);
    }

    @Override
    public List<KnowledgePoints> findAll() {
        return knowledgePointsDao.findAll();
    }

    @Override
    public Map<String, Long> getKnowledgePointNameMap() {
        Map<String, Long> resultMap = new HashMap<>();
        List<KnowledgePoints> knowledgePointsList = knowledgePointsDao.findAll();
        if (!ObjectUtils.isEmpty(knowledgePointsList)) {
            knowledgePointsList.forEach(knowledgePoints -> {
                resultMap.put(knowledgePoints.getPointName(), knowledgePoints.getId());
            });
        }
        return resultMap;
    }

    @Override
    public Map<Long, String> getKnowledgePointIdMap() {
        Map<Long, String> resultMap = new HashMap<>();
        List<KnowledgePoints> knowledgePointsList = knowledgePointsDao.findAll();
        if (!ObjectUtils.isEmpty(knowledgePointsList)) {
            knowledgePointsList.forEach(knowledgePoints -> {
                resultMap.put(knowledgePoints.getId(), knowledgePoints.getPointName());
            });
        }
        return resultMap;
    }


    /**
     * @author:胡立涛
     * @description: TODO 删除知识点及子知识点信息
     * @date: 2021/12/9
     * @param: [knowledgePoints]
     * @return: void
     */
    @Transactional
    @Override
    public void delClass(KnowledgePoints knowledgePoints) {
        List<Map> aClass=null;
        if (dbType==1){
            aClass = knowledgePointsDao.getClassByIdDM(knowledgePoints.getId());
        }else {
            aClass = knowledgePointsDao.getClassById(knowledgePoints.getId());
        }
        if (!StringUtils.isEmpty(aClass)) {
            for (Map map : aClass) {
                knowledgePointsDao.delete(Long.valueOf(map.get("id").toString()));
            }
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 批量 知识点数据同步
     * @date: 2022/2/18
     * @param: [list]
     * @return: void
     */
    @Override
    @Transactional
    public void bathPoint(List<Map> list) {
        for (Map map : list) {
            String kpCode = map.get("kpCode").toString();
            String parentCode = map.get("parentCode") == null ? "0" : map.get("parentCode").toString();
            if (parentCode == "null") {
                parentCode = "0";
            }
            String pointName = map.get("pointName").toString();
            String kpLabel = map.get("kpLabel") == null ? "no-root" : map.get("kpLabel").toString();
            if (kpLabel.equals(rootLabel) || kpLabel == rootLabel) {
                // 将表中的数据更新为非根节点
                knowledgePointsDao.updateKpLabel(rootLabel);
            }
            // 根据kpCode查询数据
            Map knowledgePointsByCodeMap = knowledgePointsDao.getKnowledgePointsByCode(kpCode);
//            knowledgePointsByCodeMap = CollectionsCustomer.builder().build().mapToLowerCase(knowledgePointsByCodeMap);
            if (knowledgePointsByCodeMap == null) {
                // 新增知识点
                KnowledgePoints knowledgePoints = new KnowledgePoints();
                knowledgePoints.setCode(kpCode);
                knowledgePoints.setParentCode(parentCode);
                knowledgePoints.setStatus(1);
                knowledgePoints.setPointName(pointName);
                knowledgePoints.setKpLabel(kpLabel);
                knowledgePoints.setCreateTime(new Timestamp(System.currentTimeMillis()));
                knowledgePoints.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                knowledgePoints.setParentId(0L);
                knowledgePointsDao.savePoint(knowledgePoints);
            } else {
                // 更新知识点
                KnowledgePoints bean = knowledgePointsDao.findById(Long.valueOf(knowledgePointsByCodeMap.get("id").toString()));
                bean.setPointName(pointName);
                bean.setParentCode(parentCode);
                bean.setKpLabel(kpLabel);
                bean.setParentId(0L);
                bean.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                knowledgePointsDao.updatePoint(bean);
            }
        }
        // 更新数据
        List<Map> infoList = knowledgePointsDao.getUpdateInfo();
//        infoList = CollectionsCustomer.builder().build().listMapToLowerCase(infoList);
        for (Map map : infoList) {
            String parentCode = map.get("parent_code").toString();
            Map knowledgePointsByCode = knowledgePointsDao.getKnowledgePointsByCode(parentCode);
            if (knowledgePointsByCode == null) {
                continue;
            }
//            knowledgePointsByCode = CollectionsCustomer.builder().build().mapToLowerCase(knowledgePointsByCode);
            KnowledgePoints knowledgePoints = new KnowledgePoints();
            knowledgePoints.setId(Long.valueOf(map.get("id").toString()));
            knowledgePoints.setParentId(Long.valueOf(knowledgePointsByCode.get("id").toString()));
            knowledgePointsDao.updateParentId(knowledgePoints);
        }
    }

    @Override
    public Map<Long, String> getKnowledgePointIdMapByIds(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<KnowledgePoints> knowledgePoints = knowledgePointsDao.selectBatchIds(ids);
        return knowledgePoints.stream().collect(Collectors.toMap(KnowledgePoints::getId, KnowledgePoints::getPointName));
    }
}
