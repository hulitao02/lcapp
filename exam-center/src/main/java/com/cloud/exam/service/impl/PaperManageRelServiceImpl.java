package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.exam.dao.PaperManageRelDao;
import com.cloud.exam.model.exam.PaperManageRel;
import com.cloud.exam.model.exam.Question;
import com.cloud.exam.model.exam.QuestionManage;
import com.cloud.exam.model.exam.QuestionTransfer;
import com.cloud.exam.service.PaperManageRelService;
import com.cloud.exam.service.PaperService;
import com.cloud.exam.service.QuestionManageService;
import com.cloud.exam.service.QuestionService;
import com.cloud.utils.Validator;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author meidan
 */
@Service
public class PaperManageRelServiceImpl extends ServiceImpl<PaperManageRelDao, PaperManageRel> implements PaperManageRelService {

    @Autowired
    private PaperManageRelDao paperManageRelDao;
    @Autowired
    private PaperService paperService;
    @Autowired
    private QuestionManageService questionManageService;
    @Autowired
    private QuestionService questionService;

    // 文件服务器地址
    @Value(value = "${file_server}")
    private String fileServer;
    // 影像缩略图访问地址
    @Value("${localUrlPrefix}")
    private String localUrlPrefix;

    @Override
    public ApiResult updatePaperManageById(PaperManageRel paperManage) {
        //此处分两种业务 1，替换试卷中的简答试题 2，直接修改简答试题中的判题答案

        boolean success;
        if (Validator.isEmpty(paperManage.getQuestionId())) {
            //直接修改简答试题中的判题答案后，同时修改试题表中的答案字段数据
            PaperManageRel paperManageRel = paperManageRelDao.selectById(paperManage.getId());
            Question question = questionService.getById(paperManageRel.getQuestionId());
            question.setAnswer(paperManage.getScoreBasis());
            questionService.saveOrUpdate(question);
            success = this.updateById(paperManage);

        } else {
            QuestionManage questionManage = questionManageService.getById(paperManage.getQuestionId());
            if (questionManage == null) {
                return ApiResultHandler.buildApiResult(500, "找不到试题", paperManage.getQuestionId());
            }
            QuestionTransfer questionTransfer = paperService.transferQuestion(Arrays.asList(questionManage));
            //替换试卷中的简答试题后判题依据同时修改
            paperManage.setScoreBasis(questionManage.getAnswer());
            paperManage.setQuestionId(questionTransfer.getQuestionList().get(0).getId());
            QueryWrapper<PaperManageRel> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("paper_id", paperManage.getPaperId());
            queryWrapper.eq("question_id", paperManage.getQuestionId());
            List<Object> list = paperManageRelDao.selectObjs(queryWrapper);
            if (CollectionUtils.isEmpty(list)) {
                success = this.updateById(paperManage);
            } else {
                success = true;
            }
        }
        return ApiResultHandler.buildApiResult(200, "修改试卷成功", success);
    }

    @Override
    public List<PaperManageRel> findByPaperId(Long paperId) {
        return paperManageRelDao.findByPaperId(paperId);
    }


    @Override
    public List<Question> findPaperDetail(Long paperId) {
        List<Question> questionList = new ArrayList<>();
        List<PaperManageRel>  paperManageRelList = this.findByPaperId(paperId);
        for(PaperManageRel paperManageRel:paperManageRelList){
            //获取问题id
            Question question = questionService.getById(paperManageRel.getQuestionId());
            question.setRelId(paperManageRel.getId());
            question.setScore(paperManageRel.getScore());
            question.setScoreBasis(paperManageRel.getScoreBasis());
            question.setPdType(question.getPdType());
            question.setFileAddr(fileServer);
            question.setLocalUrlPrefix(localUrlPrefix);
            questionList.add(question);
        }

        return questionList;
    }


}
