package com.cloud.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.exam.model.exam.QuestionError;

import java.util.Map;

public interface QuestionErrorService extends IService<QuestionError> {

    IPage<QuestionError> findAll(Page<QuestionError> page, Map<String,Object> params);

    /**
     *
     * @author: 胡立涛
     * @description: TODO 知识点删除
     * @date: 2022/5/23
     * @param: [kpIds]
     * @return: void
     */
    void delKnowledgePoint(Long[] kpIds);


    /**
     *
     * @author: 胡立涛
     * @description: TODO 知识删除
     * @date: 2022/5/25
     * @param: [kIds]
     * @return: void
     */
    void delKnowledge(String[] kIds);

}
