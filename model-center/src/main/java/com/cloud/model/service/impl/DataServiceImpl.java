package com.cloud.model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.model.dao.KnowledgeProDao;
import com.cloud.model.dao.ModelDataDao;
import com.cloud.model.dao.ModelKpDao;
import com.cloud.model.model.KnowledgePro;
import com.cloud.model.service.DataService;
import com.cloud.utils.CollectionsCustomer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DataServiceImpl implements DataService {

    @Autowired
    ModelDataDao modelDataDao;
    @Autowired
    KnowledgeProDao knowledgeProDao;
    @Autowired
    ModelKpDao modelKpDao;
    @Autowired
    ManageBackendFeign manageBackendFeign;

    /**
     * @author:胡立涛
     * @description: TODO 删除属性
     * @date: 2022/1/25
     * @param: [map]
     * @return: void
     */
    @Transactional
    public void delProName(Map<String, Object> map) {
        knowledgeProDao.delInfoByProCode(map.get("proCode").toString());
        modelDataDao.delInfoByProCode(map.get("proCode").toString());
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据属性code，知识点id删除对应的数据
     * @date: 2022/8/8
     * @param: [map]
     * @return: void
     */
    @Transactional
    public void delProNameAndKpCode(Map<String, Object> map) {
        modelDataDao.delInfoByProCodeAndKpId(map);
        knowledgeProDao.delInfoByProCodeAndKpId(map);

    }

    /**
     * @author:胡立涛
     * @description: TODO 删除属性分组
     * @date: 2021/12/9
     * @param: [map]
     * @return: void
     */
    @Transactional
    public void delProTypeName(Map<String, Object> map) {
        modelDataDao.delInfoByProTypeCode(map.get("proTypeCode").toString());
        knowledgeProDao.delInfoByProTypeCode(map.get("proTypeCode").toString());
    }


    /**
     * @author:胡立涛
     * @description: TODO 删除知识点及知识点下的知识点关联关系
     * @date: 2021/12/9
     * @param: [list]
     * @return: void
     */
    @Override
    @Transactional
    public void delClass(List<Map> list) {
        if (!StringUtils.isEmpty(list)) {
            for (Map map : list) {
                long kpId = Long.valueOf(map.get("id").toString());
                // 根据知识点id删除知识点属性 knowledge_pro表
                QueryWrapper queryWrapper = new QueryWrapper();
                queryWrapper.eq("kp_id", kpId);
                knowledgeProDao.delete(queryWrapper);
                // 根据知识点id删除模板与知识点关系 model_kp表
                modelKpDao.delete(queryWrapper);
                // 根据知识点id删除数据绑定关系 model_data表
                modelDataDao.delete(queryWrapper);
            }
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 批量 知识点属性数据同步
     * @date: 2022/2/18
     * @param: [list]
     * @return: void
     */
    @Override
    @Transactional
    public void bathPro(List<Map> list) {
        for (Map map : list) {
            String kpCode = map.get("kpCode").toString();
            String proCode = map.get("proCode").toString();
            String proTypeCode = map.get("proTypeCode").toString();
            String proName = map.get("proName").toString();
            String proTypeName = map.get("proTypeName").toString();
            // 根据知识点code，属性code查询信息
            QueryWrapper<KnowledgePro> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("kp_code", kpCode);
            queryWrapper.eq("pro_code", proCode);
            queryWrapper.eq("is_property", 1);
            List<KnowledgePro> proList = knowledgeProDao.selectList(queryWrapper);
            if (!proList.isEmpty() && proList.size()>1){
                knowledgeProDao.deleteById(proList.get(0).getId());
            }
            KnowledgePro knowledgePro = knowledgeProDao.selectOne(queryWrapper);
            if (knowledgePro == null) {
                // 新增知识点属性
                knowledgePro = new KnowledgePro();
                knowledgePro.setKpCode(kpCode);
                knowledgePro.setProCode(proCode);
                knowledgePro.setProTypeCode(proTypeCode);
                knowledgePro.setProProname(proName);
                knowledgePro.setProTypeName(proTypeName);
                knowledgePro.setStatus(1);
                knowledgePro.setIsProperty(1);
                // 根据知识点code查询知识点id
                Map knowledgePointsByCode = manageBackendFeign.getKnowledgePointsByCode(kpCode);
                if (knowledgePointsByCode==null){
                    log.info("------不存在该知识点数据（knowledge_point表):"+kpCode);
                    continue;
                }
//                knowledgePointsByCode = CollectionsCustomer.builder().build().mapToLowerCase(knowledgePointsByCode);
                knowledgePro.setKpId(Long.valueOf(knowledgePointsByCode.get("id").toString()));
                knowledgePro.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                knowledgeProDao.insert(knowledgePro);
            } else {
                // 更新知识点属性
                knowledgePro.setProProname(proName);
                knowledgePro.setProTypeCode(proTypeCode);
                knowledgePro.setProTypeName(proTypeName);
                knowledgePro.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                knowledgeProDao.updateById(knowledgePro);
            }
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 批量 知识点关系数据同步
     * @date: 2022/2/18
     * @param: [list]
     * @return: void
     */
    @Override
    @Transactional
    public void bathRelation(List<Map> list) {
        for (Map map : list) {
            String kpCode = map.get("kpCode").toString();
            String proCode = map.get("proCode").toString();
            String proTypeCode = map.get("proTypeCode").toString();
            String proName = map.get("proName").toString();
            String proTypeName = map.get("proTypeName").toString();
            // 根据知识点code，属性code，关系code查询数据
            QueryWrapper<KnowledgePro> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("kp_code", kpCode);
            queryWrapper.eq("pro_code", proCode);
            queryWrapper.eq("pro_type_code", proTypeCode);
            queryWrapper.eq("is_property", 0);

            List<KnowledgePro> proList = knowledgeProDao.selectList(queryWrapper);
            if (!proList.isEmpty() && proList.size()>1){
                knowledgeProDao.deleteById(proList.get(0).getId());
            }
            KnowledgePro knowledgePro = knowledgeProDao.selectOne(queryWrapper);
            if (knowledgePro == null) {
                // 新增关系
                knowledgePro = new KnowledgePro();
                knowledgePro.setProCode(proCode);
                knowledgePro.setProTypeCode(proTypeCode);
                knowledgePro.setProProname(proName);
                knowledgePro.setProTypeName(proTypeName);
                knowledgePro.setKpCode(kpCode);
                knowledgePro.setStatus(1);
                knowledgePro.setIsProperty(0);
                Map knowledgePointsByCode = manageBackendFeign.getKnowledgePointsByCode(kpCode);
                if (knowledgePointsByCode==null){
                    log.info("------同步关系数据：不存在该知识点数据（knowledge_point表):"+kpCode);
                    continue;
                }
//                knowledgePointsByCode = CollectionsCustomer.builder().build().mapToLowerCase(knowledgePointsByCode);
                knowledgePro.setKpId(Long.valueOf(knowledgePointsByCode.get("id").toString()));
                knowledgePro.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                knowledgeProDao.insert(knowledgePro);
            } else {
                // 更新关系
                knowledgePro.setProProname(proName);
                knowledgePro.setProTypeName(proTypeName);
                knowledgePro.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                knowledgeProDao.updateById(knowledgePro);
            }
        }
    }
}
