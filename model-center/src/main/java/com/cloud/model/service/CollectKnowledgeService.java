package com.cloud.model.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.model.CollectKnowledgeBean;

import java.util.List;
import java.util.Map;

public interface CollectKnowledgeService extends IService<CollectKnowledgeBean> {

    /**
     *  分页 查询
     * @param pageNo
     * @param pageSize
     * @param collectKnowledgeBean
     * @return
     */
    public IPage<CollectKnowledgeBean> pageUserCollectKnowledge(int pageNo, int pageSize,
                                                                CollectKnowledgeBean collectKnowledgeBean);


    /**
     *  知识学习统计
     * @param paramMap
     * @return
     */
    List<Map<String, Object>> statisticsStudyCount(Map<String, Object> paramMap);

    /**
     *  统计 学员总量排序
     * @param paramDate
     * @return
     */
    List<Map<String,Object>> knStudyUserCountGroup(String paramDate);


    /**
     * 统计前10个被学习数量最多的知识和访问人数
     * @param paramDate
     * @return
     */
    List<Map<String, Object>> getKnCountGroupbyKnId(String paramDate);



}
