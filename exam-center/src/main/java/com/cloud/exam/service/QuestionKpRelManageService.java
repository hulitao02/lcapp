package com.cloud.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.exam.model.exam.QuestionKpRelManage;

import java.util.List;
import java.util.Map;

public interface QuestionKpRelManageService extends IService<QuestionKpRelManage> {

    List<QuestionKpRelManage> findByQuestionIdList(List<Long> questionIdList);

    Map<Long, List<String>> getQuestionIdAndKpIdListMap(List<Long> idList);
}
