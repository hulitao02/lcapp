package com.cloud.model.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.model.bean.vo.ModelDataVO;
import com.cloud.model.bean.vo.ModelKpVo;
import com.cloud.model.dao.ModelDataDao;
import com.cloud.model.dao.ModelKpDao;
import com.cloud.model.dao.RelationKnowledgeDataDao;
import com.cloud.model.model.ModelData;
import com.cloud.model.model.ModelKp;
import com.cloud.model.service.ModelDataService;
import com.cloud.model.service.ModelKpService;
import com.cloud.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ModelDataServiceImpl extends ServiceImpl<ModelDataDao, ModelData> implements ModelDataService {

    @Autowired
    private ModelKpService modelKpService;
    @Autowired
    private ModelDataDao modelDataDao;
    @Autowired
    ModelKpDao modelKpDao;
    @Autowired
    RelationKnowledgeDataDao relationKnowledgeDataDao;

    @Override
    @Transactional
    public boolean add(ModelData modelData) {
        checkModelData(modelData);
        ModelKp modelKp = modelKpService.queryById(modelData.getModelKpId());
        if (modelKp == null) {
            throw new IllegalArgumentException("知识点模板不存在");
        }
        modelData.setStatus(1);
        Boolean saveFlag = save(modelData);
        log.info("ModelDataServiceImpl#add-->saveFlag={}, modelData={}", saveFlag, JSON.toJSONString(modelData));
        return saveFlag;
    }

    /**
     * 通过挂在的知识点的属性 查询 实体
     *
     * @param modelData
     * @return
     */
    @Override
    public List<ModelDataVO> getAssemblyListByTemplate(ModelData modelData) {
        List<ModelDataVO> modelDataList = this.modelDataDao.getAssemblyListByTemplate(modelData);
        return modelDataList;
    }

    @Override
    public List<ModelDataVO> getModelKpIdListBykpId(ModelData modelData) {
        return this.modelDataDao.getModelKpIdListBykpId(modelData);
    }

    private void checkModelData(ModelData modelData) {
        if (modelData.getModelKpId() == null) {
            throw new IllegalArgumentException("知识点挂载模板不能为空");
        }
        if (modelData.getKpId() == null) {
            throw new IllegalArgumentException("对接知识点不能为空");
        }
        if (StringUtils.isNotBlank(modelData.getAssemblyName())) {
            throw new IllegalArgumentException("模板组件不能为空");
        }
        if (StringUtils.isNotBlank(modelData.getProProname())) {
            throw new IllegalArgumentException("组件属性不能为空");
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 完成数据挂接
     * @date: 2021/11/29
     * @param: [id]
     * @return: void
     */
    @Override
    public void finishData(Long id) {
        ModelKpVo modelKpVoById = modelKpDao.getModelKpVoById(id);
        modelKpVoById.setStatus(2);
        modelKpDao.updateById(modelKpVoById);
    }


    @Transactional
    public void delPro(Map<String, Object> map) {
        // 根据id删除模版属性
        modelDataDao.deleteById(Integer.parseInt(map.get("id").toString()));
        // flg:是否为知识关系数据 1:是
        String flg = map.get("flg") == null ? null : map.get("flg").toString();
        if (flg != null && flg.equals("1")) {
            // 删除知识关系属性表的数据（relation_knowledge_data）
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("model_data_id", Long.valueOf(map.get("id").toString()));
            relationKnowledgeDataDao.delete(queryWrapper);
        }
        // 根据model_kp_id查询模板属性信息
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("model_kp_id", Integer.parseInt(map.get("modelKpId").toString()));
        List list = modelDataDao.selectList(queryWrapper);
        if (list == null) {
            ModelKp modelKpId = modelKpService.getById(Integer.parseInt(map.get("modelKpId").toString()));
            if (modelKpId != null) {
                modelKpId.setStatus(1);
                modelKpService.updateById(modelKpId);
            }
        }
    }
}
