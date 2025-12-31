package com.cloud.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.exam.model.exam.QuestionKpRel;

import java.util.List;

/**
 * Created by dyl on 2021/03/22.
 */
public interface QuestionKpRelService extends IService<QuestionKpRel> {


    List<QuestionKpRel> findByQuestionIdList(List<Long> questionIdList);
}
