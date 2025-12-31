package com.cloud.model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.model.StudyKnowledge;

import java.util.List;

public interface StudyKnowledgeService extends IService<StudyKnowledge> {

    List<StudyKnowledge> getStudyKnowledgeInfos(List<Integer> planIdList);

}
