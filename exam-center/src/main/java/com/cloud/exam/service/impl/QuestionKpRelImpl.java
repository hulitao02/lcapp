package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.QuestionKpRelDao;
import com.cloud.exam.model.exam.QuestionKpRel;
import com.cloud.exam.service.QuestionKpRelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by dyl on 2021/03/22.
 */
@Service
@Transactional
public class QuestionKpRelImpl extends ServiceImpl<QuestionKpRelDao, QuestionKpRel> implements QuestionKpRelService {

    @Autowired
    private QuestionKpRelDao questionKpRelDao;

    @Override
    public List<QuestionKpRel> findByQuestionIdList(List<Long> questionIdList) {
        return lambdaQuery()
                .in(QuestionKpRel::getQuestionId, questionIdList).list();
    }
}
