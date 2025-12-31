package com.cloud.model.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.model.model.FocusKp;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface FocusKpDao extends BaseMapper<FocusKp> {

    /**
     *
     * @author:胡立涛
     * @description: TODO 根据用户id，知识点id查询关注信息
     * @date: 2022/1/13
     * @param: [map]
     * @return: java.util.Map<java.lang.String,java.lang.Object>
     */
    Map<String,Object> getFocusKp(Map<String,Object> map);
}
