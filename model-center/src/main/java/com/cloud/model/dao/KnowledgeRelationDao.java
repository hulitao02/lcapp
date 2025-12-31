package com.cloud.model.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.model.model.KnowledgeRelation;
import com.cloud.model.model.KnowledgeViewBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


@Mapper
public interface KnowledgeRelationDao extends BaseMapper<KnowledgeRelation> {


    /**
     *
     * @author: 胡立涛
     * @description: TODO 根据知识code和关系知识查询信息
     * @date: 2022/5/30
     * @param: [map]
     * @return: java.util.Map
     */
    Map getRelationInfo(Map<String,Object> map);
}
