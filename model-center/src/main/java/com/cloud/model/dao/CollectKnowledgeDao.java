package com.cloud.model.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.model.model.CollectKnowledgeBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


@Mapper
public interface CollectKnowledgeDao extends BaseMapper<CollectKnowledgeBean> {

    /**
     * @author:胡立涛
     * @description: TODO 根据用户id，知识id查询收藏信息
     * @date: 2021/11/26
     * @param: [map]
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     */
    Map<String, Object> getCollectKnowledge(Map<String, Object> map);

    /**
     * 只是学习统计
     *
     * @param paramMap
     * @return
     */
    List<Map<String, Object>> statisticsStudyCount(Map<String, Object> paramMap);


    List<Map<String, Object>> knStudyUserCountGroup(String paramDate);


    /**
     * @param paramDate
     * @return
     * @ApiOperation("统计 被学习的知识次数最多的知识和，人数")
     */
    List<Map<String, Object>> getKnCountGroupbyKnId(String paramDate);

    /**
     * @author: 胡立涛
     * @description: TODO 根据知识code删除收藏知识
     * @date: 2022/5/25
     * @param: [kIds]
     * @return: void
     */
    void delCollectionKnowledge(@Param("kIds") String[] kIds);


}
