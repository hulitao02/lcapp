package com.cloud.model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.bean.vo.PageModelVo;
import com.cloud.model.dao.ModelDataDao;
import com.cloud.model.dao.ModelKpDao;
import com.cloud.model.dao.PageModelDao;
import com.cloud.model.model.ModelData;
import com.cloud.model.model.ModelKp;
import com.cloud.model.model.PageModel;
import com.cloud.model.service.ModelKpService;
import com.cloud.model.service.PageModelService;
import com.cloud.model.user.AppUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Slf4j
@Service
public class PageModelServiceImpl extends ServiceImpl<PageModelDao, PageModel> implements PageModelService {

    @Autowired
    private ModelKpService modelKpService;
    @Autowired
    private SysDepartmentFeign sysDepartmentFeign;
    @Autowired
    PageModelDao pageModelDao;
    @Autowired
    ModelDataDao modelDataDao;
    @Autowired
    ModelKpDao modelKpDao;

    @Value("${file.local.urlPrefix}")
    private String fileUrlPrefix;



    @Override
    public IPage<PageModelVo> listByPage(Long groupId, Integer pageNum, Integer pageSize) {
        Page<PageModelVo> pageModelPage = new Page<>(pageNum, pageSize);
        IPage<PageModelVo> voIPage = baseMapper.getVoListByGroupId(pageModelPage, groupId);

        if (!CollectionUtils.isEmpty(voIPage.getRecords())) {
            voIPage.getRecords().stream().forEach(m -> {
                if (StringUtils.isNotBlank(m.getPicPath())) {

                    m.setPicPath(fileUrlPrefix + m.getPicPath());
                }
                if (m.getCreator() != null) {
                    AppUser appUser = sysDepartmentFeign.findUserById(m.getCreator());
                    m.setCreatorName(appUser != null ? appUser.getUsername() : null);
                }
            });
        }
        return voIPage;
    }


    /**
     *
     * @author:胡立涛
     * @description: TODO 删除模板及关系数据
     * @date: 2021/11/30
     * @param: [id]
     * @return: java.lang.Boolean
     */

    @Override
    @Transactional
    public Boolean deleteById(Long id) {
        PageModel oldPageModel = baseMapper.selectById(id);
        if (oldPageModel == null) {
            throw new IllegalArgumentException("模板信息不存在");
        }
        // 删除模板
        pageModelDao.deleteById(id);
        List<ModelKp> modelKps = modelKpService.getByModelId(id);
        if (modelKps != null && modelKps.size() > 0) {
            // 删除模板与知识点关系
            QueryWrapper<ModelKp> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("model_id", id);
            modelKpDao.delete(queryWrapper);
            for (ModelKp bean : modelKps) {
                // 删除模版与知识点属性关系
                QueryWrapper<ModelData> dataQueryWrapper = new QueryWrapper<>();
                dataQueryWrapper.eq("model_kp_id", bean.getId());
                modelDataDao.delete(dataQueryWrapper);
            }
        }
        return true;
    }
}
