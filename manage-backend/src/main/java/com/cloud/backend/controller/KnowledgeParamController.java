package com.cloud.backend.controller;

import com.cloud.backend.dao.KnowledgeParamDao;
import com.cloud.backend.model.KnowledgeParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author: 胡立涛
 * @description: TODO 智能推荐所需参数设置表
 * @date: 2022/5/30
 * @param:
 * @return:
 */
@Slf4j
@RestController
public class KnowledgeParamController {

    @Autowired
    KnowledgeParamDao knowledgeParamDao;

    /**
     * @author: 胡立涛
     * @description: TODO 根据参数名称获取参数值
     * @date: 2022/5/30
     * @param: [paramName]
     * @return: void
     */
    @PostMapping(value = "/knowledgeparam/getParamValue")
    public String getParamValue(@RequestBody String paramName) {
        KnowledgeParam knowledgeParam = new KnowledgeParam();
        knowledgeParam.setParamName(paramName);
        return knowledgeParamDao.getParamValue(knowledgeParam);
    }
}
