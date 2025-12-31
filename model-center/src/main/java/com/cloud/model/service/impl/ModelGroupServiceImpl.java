package com.cloud.model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.model.dao.ModelDataDao;
import com.cloud.model.dao.ModelGroupDao;
import com.cloud.model.dao.ModelKpDao;
import com.cloud.model.dao.PageModelDao;
import com.cloud.model.model.ModelData;
import com.cloud.model.model.ModelGroup;
import com.cloud.model.model.ModelKp;
import com.cloud.model.model.PageModel;
import com.cloud.model.service.ModelGroupService;
import com.cloud.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class ModelGroupServiceImpl extends ServiceImpl<ModelGroupDao, ModelGroup> implements ModelGroupService {

    @Autowired
    ModelGroupDao modelGroupDao;
    @Autowired
    PageModelDao pageModelDao;
    @Autowired
    ModelKpDao modelKpDao;
    @Autowired
    ModelDataDao modelDataDao;

    @Override
    public Boolean add(String name) {
        checkName(name);
        ModelGroup modelGroup = new ModelGroup();
        modelGroup.setName(name);
        modelGroup.setStatus(1);
        return save(modelGroup);
    }

    private void checkName(String name) {
        //判断名称是否重复
        ModelGroup oldGroup = query().eq("name", name).eq("status", 1).one();
        if (oldGroup != null) {
            throw new IllegalArgumentException("名称已存在");
        }
    }

    @Override
    public Boolean updateGroup(ModelGroup modelGroup) {
        if (modelGroup.getId() == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        checkName(modelGroup.getName());
        return updateById(modelGroup);
    }


    @Transactional
    public void deleteGroup(Long id) {
        // 删除小组信息
        modelGroupDao.deleteById(id);
        QueryWrapper<PageModel> queryWrapper = new QueryWrapper<PageModel>();
        queryWrapper.eq("group_id", id);
        List<PageModel> pageModels = pageModelDao.selectList(queryWrapper);
        if (pageModels != null && pageModels.size() > 0) {
            for (PageModel bean : pageModels) {
                // 知识模版id
                Long mobanId = bean.getId();
                // 删除分组下的模版
                pageModelDao.deleteById(mobanId);
                QueryWrapper<ModelKp> kpqueryWrapper = new QueryWrapper<ModelKp>();
                kpqueryWrapper.eq("model_id", mobanId);
                List<ModelKp> modelKps = modelKpDao.selectList(kpqueryWrapper);
                for (ModelKp kpBean : modelKps) {
                    // 删除模版与知识关系
                    modelKpDao.deleteById(kpBean.getId());
                    // 删除模版与数据关系
                    QueryWrapper<ModelData> dataqueryWrapper = new QueryWrapper<ModelData>();
                    dataqueryWrapper.eq("model_kp_id", kpBean.getId());
                    modelDataDao.delete(dataqueryWrapper);
                }
            }
        }
    }

    @Override
    public List<ModelGroup> queryAll() {
        return query().eq("status", 1).list();
    }

    @Override
    public ModelGroup queryById(Long id) {
        return baseMapper.selectOne(new QueryWrapper<ModelGroup>().lambda().eq(ModelGroup::getId, id).eq(ModelGroup::getStatus, 1));
    }
}
