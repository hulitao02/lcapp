package com.cloud.backend.dao;

import com.cloud.backend.model.KnowledgeParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KnowledgeParamDao {


    /**
     *
     * @author: 胡立涛
     * @description: TODO 根据参数名获取参数值
     * @date: 2022/5/30
     * @param: [knowledgeParam]
     * @return: java.lang.String
     */
    String getParamValue(KnowledgeParam knowledgeParam);

}
