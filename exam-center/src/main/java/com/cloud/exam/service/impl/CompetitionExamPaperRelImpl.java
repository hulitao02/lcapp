package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.CompetitionExamPaperRelDao;
import com.cloud.exam.model.exam.CompetitionExamPaperRel;
import com.cloud.exam.service.CompetitionExamPaperRelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dyl on 2021/03/22.
 */
@Service
@Transactional
public class CompetitionExamPaperRelImpl extends ServiceImpl<CompetitionExamPaperRelDao,CompetitionExamPaperRel> implements CompetitionExamPaperRelService {

    @Autowired
    private CompetitionExamPaperRelDao competitionExamPaperRelDao;

}
