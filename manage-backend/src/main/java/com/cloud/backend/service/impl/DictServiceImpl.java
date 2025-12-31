package com.cloud.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.backend.dao.DictDao;
import com.cloud.backend.service.DictService;
import com.cloud.model.common.Dict;
import com.cloud.model.common.Page;
import com.cloud.utils.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DictServiceImpl extends ServiceImpl<DictDao, Dict> implements DictService {
    @Autowired
    DictDao dictDao;

    //没有逻辑事务时可以不加Transactional
    @Transactional
    @Override
    public void saveDict(Dict dict) {
        Dict sysDict = dictDao.findBydictName(dict.getDictName());
        if (sysDict != null) {
            throw new IllegalArgumentException("字典已存在");
        }
        dictDao.save(dict);
        log.info("保存字典：{}", dict);
    }

    @Transactional
    @Override
    public void update(Dict dict) {
        dictDao.update(dict);
        log.info("修改权限标识：{}", dict);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Dict dict = dictDao.findById(id);
        if (dict == null) {
            throw new IllegalArgumentException("字典不存在");
        }
        dictDao.delete(id);
        log.info("删除权限标识：{}", dict);
    }


    @Override
    public Page<Dict> findPermissions(Map<String, Object> params) {
        int total = dictDao.count(params);
        List<Dict> list = Collections.emptyList();
        if (total > 0) {
            PageUtil.pageParamConver(params, false);

            list = dictDao.findData(params);
        }
        return new Page<>(total, list);
    }

    @Override
    public List<Dict> findDict(Map<String, Object> params) {
        List<Dict> list = Collections.emptyList();
        PageUtil.pageParamConver(params, false);
        list = dictDao.findData(params);
        return list;
    }

    @Override
    public String getThemeType() {
        String va = dictDao.getThemeType() ;
        return va ;
    }

    @Override
    public void setThemeType(Long id) {
        dictDao.setThemeType(id) ;
    }

    @Override
    public List<Dict> getThemeTypeList(Map<String, Object> params) {
        List<Dict> list = Collections.emptyList();
        PageUtil.pageParamConver(params, false);
        list = dictDao.getThemeTypeList(params);
        return list ;
    }

    @Override
    public Dict getLogoDict(String logoType, String key) {
        return dictDao.getLogoDict(logoType,key);
    }

}
