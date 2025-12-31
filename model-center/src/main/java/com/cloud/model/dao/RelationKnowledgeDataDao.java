package com.cloud.model.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.model.model.RelationKnowledgeData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;


@Mapper
public interface RelationKnowledgeDataDao extends BaseMapper<RelationKnowledgeData> {

    /**
     *
     * @author:胡立涛
     * @description: TODO 查询知识关系属性数据
     * @date: 2022/9/19
     * @param: [map]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String,Object>> getList(Map<String,Object> map);
}
