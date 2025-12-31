package com.cloud.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.core.ApiResult;
import com.cloud.exam.model.exam.Question;

import java.util.List;
import java.util.Map;

public interface QuestionService extends IService<Question> {


    public IPage<Question> findByPage(Question question);

    IPage<Question> findAll(Page<Question> page, Map<String, Object> params);

    IPage<Question> findAllTest(Page<Question> page);

    ApiResult findOnlyQuestionId(Long id);


    Question[] getQuestionArray(int type, List<String> kpString, List<Long> substring, List<Long> qIds);

    List<Question> getQuestionListWithOutSId(Question tmpQuestion);

    int deleteQuestion(Question question);


    Question findByHighVersion(String code);


    boolean updateQuestion(Question question);

    List<Question> selectByDirectId(Long id);

    Double getQuestionScoreById(Long paperId, Long questionId);

    List<Question> getQuestion(List<Long> kpIds, List<Long> intDirectIds, Integer questionType, Integer questionNum, Double difficulty1, Double difficulty2, List<Long> qIds);

    Map<String, List<Question>> getQuestionList();

    boolean saveQuestion(Question question);

    void saveJson(Question qq, List<String> kpIds);

    public void saveJsonAndFiles(Question qq, List<String> kpIds);

}
