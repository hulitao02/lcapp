package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.AnalysisFrameworkDao;
import com.cloud.exam.model.exam.AnalysisFramework;
import com.cloud.exam.service.AnalysisFrameworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author meian
 */
@Service
@Transactional
public class AnalysisFrameworkServiceImpl extends ServiceImpl<AnalysisFrameworkDao, AnalysisFramework> implements AnalysisFrameworkService {

    @Autowired
    private AnalysisFrameworkDao analysisFrameworkDao ;

    @Override
    public IPage<AnalysisFramework> findAll(Page<AnalysisFramework> page, Map<String, Object> params) {
        QueryWrapper<AnalysisFramework> queryWrapper=new QueryWrapper<>();
        if(null != params.get("name") && ""!=params.get("name").toString() ){
            queryWrapper.like("name",params.get("name").toString());
        }
        if (null != params.get("type") && ""!=params.get("type").toString()){
            queryWrapper.eq("type",Integer.parseInt(params.get("type").toString()));
        }
        queryWrapper.select().orderByDesc("create_time");
        return analysisFrameworkDao.selectAnalysisFrameworkPage(page,queryWrapper);
    }
}
