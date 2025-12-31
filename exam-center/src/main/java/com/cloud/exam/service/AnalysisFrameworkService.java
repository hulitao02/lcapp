package com.cloud.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.exam.model.exam.AnalysisFramework;

import java.util.Map;


public interface AnalysisFrameworkService extends IService<AnalysisFramework> {


    IPage<AnalysisFramework> findAll(Page<AnalysisFramework> objectPage, Map<String, Object> params);

}
