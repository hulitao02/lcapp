package com.cloud.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.exam.model.exam.QuestionManage;

import java.util.List;
import java.util.Map;

public interface QuestionManageService extends IService<QuestionManage> {


    IPage<QuestionManage> findByPage(QuestionManage question);

    IPage<QuestionManage> findAll(Page<QuestionManage> page, Map<String, Object> params);

    IPage<QuestionManage> findAllTest(Page<QuestionManage> page);

    QuestionManage findOnlyQuestionId();


    QuestionManage[] getQuestionArray(int type, List<String> kpString, List<Long> substring,
                                      List<Long> qIds,List<String> pdTypes,Double diff);

    List<QuestionManage> getQuestionListWithOutSId(QuestionManage tmpQuestion);

    int deleteQuestion(QuestionManage question);


    QuestionManage findByHighVersion(String code);


    boolean updateQuestion(QuestionManage question);

    List<QuestionManage> selectByDirectId(Long id);

    Double getQuestionScoreById(Long paperId, Long questionId);

    List<QuestionManage> getQuestion(List<Long> kpIds, List<Long> intDirectIds, Integer questionType, Integer questionNum, Double difficulty1, Double difficulty2, List<Long> qIds);

    Map<String, List<QuestionManage>> getQuestionList();

    boolean saveQuestion(QuestionManage question);

    void saveJson(QuestionManage qq, List<String> kpIds);

    void saveJsonAndFiles(QuestionManage qq, List<String> kpIds);

    Map<Long, String> getQuestionIdKnowledgeNameMap(List<Long> questionIdList);
}
