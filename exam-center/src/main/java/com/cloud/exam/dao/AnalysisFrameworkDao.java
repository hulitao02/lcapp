package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.cloud.exam.model.exam.AnalysisFramework;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Created by dyl.
 */
@Mapper
public interface AnalysisFrameworkDao extends BaseMapper<AnalysisFramework> {

    IPage<AnalysisFramework> selectAnalysisFrameworkPage(IPage<AnalysisFramework> page, @Param(Constants.WRAPPER) Wrapper<AnalysisFramework> queryWrapper);
}
