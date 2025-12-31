package com.cloud.model.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.core.ApiResultHandler;
import com.cloud.model.dao.ModelControlDao;
import com.cloud.model.dao.ModelDataDao;
import com.cloud.model.dao.ModelKpDao;
import com.cloud.model.model.ModelControl;
import com.cloud.model.model.ModelKp;
import com.cloud.model.service.ModelControlService;
import com.cloud.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Service
public class ModelControlServiceImpl extends ServiceImpl<ModelControlDao, ModelControl>
        implements ModelControlService {


    @Autowired
    ModelControlDao modelControlDao;
    @Autowired
    ModelDataDao modelDataDao;
    @Autowired
    ModelKpDao modelKpDao;

    @Transactional
    @Override
    public Integer saveInfoList(Map<String, Object> map) {
        List<ModelControl> infoList = JSON.parseArray(map.get("infoList").toString(), ModelControl.class);
        for (ModelControl bean : infoList) {
            if (bean.getId() == null) {
                return 101;
            }
            if (StringUtils.isEmpty(bean.getName())) {
                return 102;
            }
            bean.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            modelControlDao.updateById(bean);
        }
        return 200;
    }


    /**
     * @author:胡立涛
     * @description: TODO 删除复杂模板子项信息
     * @date: 2022/9/26
     * @param: [modelControl]
     * @return: void
     */
    @Override
    @Transactional
    public void delModelControl(ModelControl modelControl) {
        modelControlDao.deleteById(modelControl.getId());
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("sql_str", String.valueOf(modelControl.getId()));
        modelDataDao.delete(queryWrapper);
        // 根据modelKpId查询数据
        queryWrapper = new QueryWrapper();
        queryWrapper.eq("model_kp_id", modelControl.getModelKpId());
        List list = modelDataDao.selectList(queryWrapper);
        if (list == null || list.size() == 0) {
            // 更新model_kp表的状态
            ModelKp modelKp = modelKpDao.selectById(modelControl.getModelKpId());
            if (modelKp != null) {
                modelKp.setStatus(1);
                modelKpDao.updateById(modelKp);
            }
        }
    }
}
