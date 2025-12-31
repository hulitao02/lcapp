package com.cloud.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.backend.dao.CountryDictDao;
import com.cloud.backend.dao.IntDirectDao;
import com.cloud.backend.model.CountryDict;
import com.cloud.backend.service.IntDirectService;
import com.cloud.model.common.IntDirect;
import com.cloud.model.common.Page;
import com.cloud.utils.PageUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class IntDirectServiceImpl extends ServiceImpl<IntDirectDao, IntDirect> implements IntDirectService {
    @Resource
    private IntDirectDao intDirectDao;
    @Resource
    private CountryDictDao countryDao;

    @Override
    public int update(IntDirect intDirect) {
        return intDirectDao.update(intDirect);
    }

    @Override
    public int delete(Long id) {
        return intDirectDao.delete(id);
    }

    @Override
    public IntDirect findById(Long id) {
        return intDirectDao.findById(id);
    }

    @Override
    public IntDirect findByName(String name) {
        return intDirectDao.findByName(name);
    }

    @Override
    public Page<IntDirect> findByPage(Map<String,Object> params){
        int total = intDirectDao.count(params);
        List<IntDirect> intDirectList = Collections.emptyList();
        if (total>0){
            PageUtil.pageParamConver(params,true);
            intDirectList = intDirectDao.findData(params);
        }
        return new Page<>(total,intDirectList);
    }

    @Override
    public List<IntDirect> findDirectCountryAll() {
        return intDirectDao.findData(null);
    }

    @Override
    public List<CountryDict> findAll() {
        return countryDao.findAll();
    }


}
