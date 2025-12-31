package com.cloud.exam.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.core.ExamConstants;
import com.cloud.exam.dao.TrainDao;
import com.cloud.exam.model.exam.DrawResult;
import com.cloud.exam.model.exam.Exam;
import com.cloud.exam.model.exam.Paper;
import com.cloud.exam.model.exam.Question;
import com.cloud.exam.model.train.Train;
import com.cloud.exam.service.*;
import com.cloud.exam.vo.RuleBeanVO;
import com.cloud.exam.vo.StudentAnswerVO;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.utils.RandomCharOrNumUtils;
import com.cloud.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author meian
 */
@Service
@Transactional
public class TrainServiceImpl extends ServiceImpl<TrainDao, Train> implements TrainService {

    @Autowired
    private ExamService examService;
    @Autowired
    private PaperService paperService;
    @Autowired
    private DrawResultService drawResultService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private QuestionService questionService;

    @Override
    public Boolean saveSelfTrain(RuleBeanVO rule) {

        Boolean f = false;

        Paper paper = new Paper();
        paper.setPaperName(rule.getPaperName());
        paper.setDescribe(rule.getPaperDescribe());
        paper.setTotalTime(rule.getPaperTime());
        paper.setType(rule.getPaperType());
        paper.setTotalScore(rule.getTotalMark());
        paper = paperService.paperSave(rule, paper);
        //保存训练信息
        try{
            Exam exam = new Exam();
            exam.setName(rule.getPaperName());
            exam.setCreateTime(new Date());
            exam.setStartTime(new Date());
            Date date = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.DAY_OF_YEAR,1);

            exam.setEndTime(c.getTime());
            exam.setCreator(AppUserUtil.getLoginAppUser().getId());
            exam.setType(ExamConstants.EXAM_TYPE_ZICE);
            exam.setIsFix(0);
            exam.setExamStatus(ExamConstants.ACTIVITY_EXAM_START);
            examService.save(exam);
            //创建一个抽签记录表
            DrawResult drawResult = new DrawResult();
            drawResult.setAcId(exam.getId());
            drawResult.setPlaceId(0);
            drawResult.setUserId(AppUserUtil.getLoginAppUser().getId());
            drawResult.setDepartId(AppUserUtil.getLoginAppUser().getDepartmentId());
            drawResult.setPaperId(paper.getId());
            drawResult.setPaperType(ExamConstants.EXAM_TYPE_LILUN);
            drawResult.setIdentityCard(RandomCharOrNumUtils.getCharAndNum(12));
            drawResult.setExamType(ExamConstants.EXAM_TYPE_ZICE);
            drawResult.setUserStatus(ExamConstants.ACTIVITY_NOT_LAUNCH);
            drawResultService.save(drawResult);
            List<Question> list = examService.getAllQuestionsBypaperId(paper.getId());
            List<StudentAnswerVO> ll = new ArrayList<>();
            for (Question q : list) {
                StudentAnswerVO vo = new StudentAnswerVO();
                vo.setQuestionId(q.getId());
                vo.setQuestion(q.getQuestion());
                vo.setOptions(q.getOptions());
                vo.setType(q.getType());
                vo.setPaperId(paper.getId());
                vo.setModelUrl(q.getModelUrl());
                JSONObject jso = new JSONObject();
                jso.put("text", "");
                if (q.getType() == 4) {
                    String options = q.getOptions();
                    JSONObject jsonObject = JSONObject.parseObject(options);
                    int size = jsonObject.keySet().size();
                    String[] s = new String[size];
                    for (int i = 0; i < s.length; i++) {
                        s[i] = "";
                    }
                    jso.put("text", s);
                }
                vo.setStuAnswer(jso.toJSONString());
                Double questionScoreById = questionService.getQuestionScoreById(paper.getId(), q.getId());
                vo.setQuestionScore(questionScoreById);
                ll.add(vo);
            }
            redisUtils.lSet("exam:" + exam.getId() + ":" + paper.getId(), ll);
            f = true;
        }catch (Exception e){
            e.printStackTrace();
            //手动回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return f;
    }
}
