package com.cloud.model.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.model.model.StudyKnowledge;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface StudyKnowledgeDao extends BaseMapper<StudyKnowledge> {

    public List<StudyKnowledge> getStudyKnowledgeInfos(List<Integer> planIdList);

    /**
     * @author: 胡立涛
     * @description: TODO 根据知识id删除学习知识信息
     * @date: 2022/5/25
     * @param: [kIds]
     * @return: void
     */
    void delStudyKnowledge(Map map);
}
