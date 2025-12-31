package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.CompetitionPaperQuestionScoreDao;
import com.cloud.exam.model.exam.CompetitionPaperQuestionScore;
import com.cloud.exam.service.CompetitionPaperQuestionScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dyl on 2021/03/22.
 */
@Service
@Transactional
public class CompetitionPaperQuestionScoreImpl extends ServiceImpl<CompetitionPaperQuestionScoreDao,CompetitionPaperQuestionScore> implements CompetitionPaperQuestionScoreService {

    @Autowired
    private CompetitionPaperQuestionScoreDao competitionPaperQuestionScoreDao;

}
