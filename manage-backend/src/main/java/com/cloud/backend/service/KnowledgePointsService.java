package com.cloud.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.common.KnowledgePoints;
import com.cloud.model.common.Page;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface KnowledgePointsService extends IService<KnowledgePoints> {

    int saveKnowledgePoints(KnowledgePoints knowledgePoints);

    int update(KnowledgePoints knowledgePoints);

    int delete(Long id);

    KnowledgePoints findById(Long id);

    KnowledgePoints findByName(String pointName);

    Page<KnowledgePoints> findByPage(Map<String, Object> params);

    List<KnowledgePoints> findAll();

    /**
     * 查询可用知识点的所有信息
     *
     * @return 知识点名称，知识点id
     */
    Map<String, Long> getKnowledgePointNameMap();

    /**
     * 查询可用知识点的所有信息
     *
     * @return 知识点名称，知识点id
     */
    Map<Long, String> getKnowledgePointIdMap();

    /**
     * @author:胡立涛
     * @description: TODO 删除知识点及知识点下的知识点
     * @date: 2021/12/9
     * @param: [knowledgePoints]
     * @return: void
     */
    void delClass(KnowledgePoints knowledgePoints);

    /**
     * @author:胡立涛
     * @description: TODO 批量 知识点数据同步
     * @date: 2022/2/18
     * @param: [list]
     * @return: void
     */
    void bathPoint(List<Map> list);

    Map<Long, String> getKnowledgePointIdMapByIds(Collection<Long> ids);
}
