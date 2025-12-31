package com.cloud.model.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.model.model.KnowledgeViewBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


@Mapper
public interface KnowledgeViewDao extends BaseMapper<KnowledgeViewBean> {


    /**
     * @author:胡立涛
     * @description: TODO 根据知识id，用户id，查询浏览信息
     * @date: 2022/1/12
     * @param: [map]
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     */
    Map<String, Object> getKnowledgeView(Map<String, Object> map);


    /**
     * @author:胡立涛
     * @description: TODO 查询所有已学知识的总量
     * @date: 2022/2/14
     * @param: []
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getStudyKnowledgeCount();


    List<Map<String, Object>> statisticsStudyCount(Map<String, Object> paramMap);

    /**
     * @author:胡立涛
     * @description: TODO 统计用户top10 知识学习
     * @date: 2022/2/24
     * @param: [year]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    List<Map<String, Object>> getUserStudyCountGroupbyUId(Map<String,Object> paramMap);


    /**
     *   统计 知识被学习的次数最多的 (热点)
     * @param paramMap
     * @return
     */
    List<Map<String, Object>> getUserCountByKn(Map<String,Object> paramMap);


    /**
     * @author: 胡立涛
     * @description: TODO 根据知识code删除knowledgeView信息
     * @date: 2022/5/28
     * @param: [kIds]
     * @return: void
     */
    void delKnowledgeView(@Param("kIds") String[] kIds);


    /**
     *
     * @author: 胡立涛
     * @description: TODO 查询用户下的所有浏览知识
     * @date: 2022/5/30
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getKnowledgeList(Map<String,Object> map);
}
