package com.cloud.user.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.user.model.LcTask;
import org.apache.ibatis.annotations.Mapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;


@Mapper
public interface TaskDao extends BaseMapper<LcTask> {

    List<Map> selectLcTaskList(Map map);


}
