package com.cloud.model.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.model.KnowledgeViewBean;

import java.util.List;
import java.util.Map;

public interface KnowledgeViewService extends IService<KnowledgeViewBean> {

    /**
     *
     * @author:胡立涛
     * @description: TODO 浏览数据分页查询
     * @date: 2022/1/13
     * @param: [pageNo, pageSize, knowledgeViewBean]
     * @return: com.baomidou.mybatisplus.core.metadata.IPage<com.cloud.model.model.KnowledgeViewBean>
     */
    IPage<KnowledgeViewBean> pageUserKnowledgeView(int pageNo, int pageSize,
                                                             KnowledgeViewBean knowledgeViewBean);

    List<Map<String, Object>> statisticsStudyCount(Map<String, Object> paramMap);


    /**
     *
     * @author: 胡立涛
     * @description: TODO 删除知识学习中的知识
     * @date: 2022/5/28
     * @param: [kIds]
     * @return: void
     */
    void delKnowledge(String[] kIds);
}
