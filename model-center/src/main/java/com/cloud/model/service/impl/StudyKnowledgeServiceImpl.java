package com.cloud.model.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.model.dao.StudyKnowledgeDao;
import com.cloud.model.model.StudyKnowledge;
import com.cloud.model.service.StudyKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudyKnowledgeServiceImpl extends ServiceImpl<StudyKnowledgeDao, StudyKnowledge>
        implements StudyKnowledgeService {

    @Autowired
    private StudyKnowledgeDao studyKnowledgeDao;

    @Override
    public List<StudyKnowledge> getStudyKnowledgeInfos(List<Integer> planIdList) {
        return this.studyKnowledgeDao.getStudyKnowledgeInfos(planIdList);
    }
}
