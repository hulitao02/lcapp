package com.cloud.exam.controller;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.core.ExamConstants;
import com.cloud.exam.annotation.RepeateRequestAnnotation;
import com.cloud.exam.model.exam.*;
import com.cloud.exam.service.*;
import com.cloud.exam.utils.GetQuestionMessageUtils;
import com.cloud.exam.utils.ListUtils;
import com.cloud.exam.vo.CompetitionExamPaperRelVO;
import com.cloud.exam.vo.QuestionTypeVO;
import com.cloud.exam.vo.QuestionVO;
import com.cloud.exam.vo.RuleBeanVO;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.thread.TaskThreadPoolConfig;
import com.cloud.thread.ThreadPoolUtils;
import com.cloud.utils.RedisUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * Created by dyl on 2021/06/16.
 * 竞答活动试卷处理类
 */
@Api(value = "竞答活动试卷处理类")
@RestController
@RequestMapping("/competition")
public class CompetitionController {

    private Logger logger = LoggerFactory.getLogger(CompetitionController.class);

    @Autowired
    private ExamService examService;
    @Autowired
    private PaperService paperService;
    @Autowired
    private CompetitionExamPaperRelService competitionExamPaperRelService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private PaperManageRelService paperManageRelService;
    @Autowired
    private CompetitionPaperQuestionScoreService competitionPaperQuestionScoreService;
    @Autowired
    private TaskThreadPoolConfig taskThreadPoolConfig;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private SysDepartmentFeign sysDepartmentFeign;

    /**
     * 获取竞答活动下的试卷
     */
    @ApiOperation(value = "获取竞答活动下的试卷")
    @ApiImplicitParam(name = "examId",value = "活动id",dataType = "Long")
    @RequestMapping("/getCompetitionPaper")
    public ApiResult getCompetitionPaper(Long examId) {
        QueryWrapper<CompetitionExamPaperRel> qw = new QueryWrapper<>();
        qw.eq("ac_id", examId);
        qw.orderByAsc("paper_sort");
        List<CompetitionExamPaperRel> list = competitionExamPaperRelService.list(qw);
        List<CompetitionExamPaperRelVO> ll = new ArrayList<>();
        for (CompetitionExamPaperRel p : list) {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("paper_id",p.getPaperId());
            List list1 = paperManageRelService.list(queryWrapper);
            CompetitionExamPaperRelVO v = new CompetitionExamPaperRelVO();
            BeanUtils.copyProperties(p, v);
            v.setPaperName(paperService.getById(p.getPaperId()).getPaperName());
            v.setPaperScore(paperService.getById(p.getPaperId()).getTotalScore());
            v.setPaperQuestionNum(list1.size());
            Object o = redisUtils.get("competition:questionIndex:" + examId + ":" + p.getPaperId());
            if(o==null){
              v.setQuestionIndex(1);
            }else{
               v.setQuestionIndex(Integer.valueOf(o.toString()));
            }
            ll.add(v);
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取试卷成功。。。", ll);
    }

    @ApiOperation(value = "对试卷进行重排序")
    @RequestMapping(value = "changePaperSort",method = RequestMethod.POST)
    public ApiResult changePaperSort(@RequestParam Long examId,@RequestBody List<Long> ids){
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("ac_id",examId);
        List<CompetitionExamPaperRel> list = competitionExamPaperRelService.list(queryWrapper);
        int i = 1;
        for (Long id:ids) {
            list.stream().filter(c->c.getId().equals(id)).findFirst().get().setPaperSort(i);
            i++;
        }
        competitionExamPaperRelService.saveOrUpdateBatch(list);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "更改试卷顺序成功。。。", null);
    }
    @ApiOperation(value = "对试卷中的试题进行重排序")
    @RequestMapping(value = "changePaperQuestionSort",method = RequestMethod.POST)
    public ApiResult changePaperQuestionSort(@RequestParam Long paperId,@RequestBody List<Long> questionIds){
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("paper_id",paperId);
        List<PaperManageRel> list = paperManageRelService.list(queryWrapper);
        int i = 1;
        for (Long questionId:questionIds) {
            list.stream().filter(c->c.getQuestionId().equals(questionId)).findFirst().get().setSort(i);
            i++;
        }
        paperManageRelService.saveOrUpdateBatch(list);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "更改试题顺序成功。。。", null);
    }


    //手动添加试题到竞答试卷中
    @ApiOperation(value = "手动添加试题到竞答试卷中")
    @RequestMapping(value = "/addPaperQuestionRel",method = RequestMethod.POST)
    public ApiResult addPaperQuestionRel(@RequestParam Long paperId,@RequestBody List<Question> questions) {
        if(CollectionUtils.isEmpty(questions)){
            throw new IllegalArgumentException("请选择试题。。。");
        }
        Paper paper  = paperService.getById(paperId);
        Double totalScore = 0.0;
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("paper_id",paperId);
        List<PaperManageRel> pmrList = paperManageRelService.list(queryWrapper);
        Map<Integer, List<Question>> collect = questions.stream().collect(Collectors.groupingBy(Question::getType));
        QueryWrapper<CompetitionExamPaperRel> qq = new QueryWrapper<>();
        qq.eq("paper_id",paperId);
        CompetitionExamPaperRel competitionExamPaperRel = competitionExamPaperRelService.list(qq).get(0);
        Integer generateType = competitionExamPaperRel.getGenerateType();
        int i = pmrList.size()+1;
        if(generateType==2){
            CompetitionPaperQuestionScore competitionPaperQuestionScore = competitionPaperQuestionScoreService.getOne(queryWrapper);
            Double simpleScore = competitionPaperQuestionScore.getDifficultyLevel1();
            Double nomalScore = competitionPaperQuestionScore.getDifficultyLevel2();
            Double middleScore = competitionPaperQuestionScore.getDifficultyLevel3();
            Double lessHardScore = competitionPaperQuestionScore.getDifficultyLevel4();
            Double moreHardScore = competitionPaperQuestionScore.getDifficultyLevel5();
            for (Integer type:collect.keySet()) {
                for (Question q:collect.get(type)) {
                    Double qScore ;
                    Double aDouble = q.getDifficulty();
                    if(aDouble< ExamConstants.difficulty_1){
                        totalScore = NumberUtil.add(totalScore,simpleScore);
                        qScore = simpleScore;
                    }else if(aDouble>=ExamConstants.difficulty_1 && aDouble<ExamConstants.difficulty_2){
                        totalScore = NumberUtil.add(totalScore,nomalScore);
                        qScore = nomalScore;
                    }else if(aDouble>=ExamConstants.difficulty_2 && aDouble<ExamConstants.difficulty_3){
                        totalScore = NumberUtil.add(totalScore,middleScore);
                        qScore = middleScore;
                    }else if(aDouble>=ExamConstants.difficulty_3 && aDouble<ExamConstants.difficulty_4){
                        totalScore = NumberUtil.add(totalScore,lessHardScore);
                        qScore = lessHardScore;
                    }else{
                        totalScore = NumberUtil.add(totalScore,moreHardScore);
                        qScore = moreHardScore;
                    }
                    PaperManageRel pmr  = new PaperManageRel();
                    pmr.setPaperId(paperId);
                    pmr.setQuestionId(q.getId());
                    pmr.setScore(qScore);
                    pmr.setSort(i);
                    pmr.setScoreBasis(q.getAnswer());
                    pmr.setQuestionTime(competitionPaperQuestionScore.getQuestionTime());
                    try{
                        paperManageRelService.save(pmr);
                        i++;
                    }catch (Exception e){
                        logger.info("id为"+q.getId()+"的试题已存在，不能重复添加。");
                    }
                }
            }
            paper.setTotalScore(totalScore);
            paper.setTotalTime(paper.getTotalTime()*i);
            paperService.saveOrUpdate(paper);
        }else {
            //随机选题 or 分难度选题
            Paper paper1 = paperService.getById(paperId);
            Question question = questions.get(0);
            Integer type = question.getType();
            double s = 1 ;
            Integer time = 1 ;
            for (PaperManageRel pmr:pmrList) {
                    Question question1 = questionService.getById(pmr.getQuestionId());
                    if(question1.getType().equals(type)){
                        s = pmr.getScore();
                        time = pmr.getQuestionTime();
                        break;
                    }
            }
            PaperManageRel pmr  = new PaperManageRel();
            pmr.setPaperId(paperId);
            pmr.setQuestionId(question.getId());
            pmr.setScore(s);
            pmr.setSort(i);
            pmr.setScoreBasis(question.getAnswer());
            pmr.setQuestionTime(time);
            try{
                paperManageRelService.save(pmr);
            }catch (Exception e){
                logger.info("id为"+question.getId()+"的试题已存在，不能重复添加。");
            }
            double ts = NumberUtil.add(paper1.getTotalScore(),s);
            paper.setTotalScore(ts);
            paper.setTotalTime(paper.getTotalTime()+time);
            paperService.saveOrUpdate(paper);
        }
        return ApiResultHandler.buildApiResult(200, "添加试题成功", null);
    }

    class GetQuestionThread implements Callable {
        //知识点
        public List<Long> kpIds;
        //情报方向
        public List<Long> intDirectIds;
        public Integer questionType;
        public Integer questionNum;
        public Double difficulty1;
        public Double difficulty2;
        public Double questionScore;
        public List<Long> qIds;
        //难度等级
        //private Integer difficultyType;

        public GetQuestionThread(List<Long> kpIds, List<Long> intDirectIds, Integer questionType, Integer questionNum, Double difficulty1, Double difficulty2,Double questionScore,List<Long> qIds) {
            //this.difficultyType = difficultyType;
            this.kpIds = kpIds;
            this.intDirectIds = intDirectIds;
            this.questionType = questionType;
            this.questionNum = questionNum;
            this.difficulty1 = difficulty1;
            this.difficulty2 = difficulty2;
            this.questionScore = questionScore;
            this.qIds = qIds;
        }

        @Override
        public Map<String,List<QuestionVO>> call() throws Exception {
            List<Question> list = questionService.getQuestion(kpIds, intDirectIds, questionType, questionNum, difficulty1, difficulty2,qIds);
            Map<String,List<QuestionVO>> ll = new HashMap<>();
            List<QuestionVO> ls = new ArrayList<>();
            for (Question q : list) {
                QuestionVO qv = new QuestionVO();
                BeanUtils.copyProperties(q, qv);
                qv.setDifficultyType(difficulty2);
                qv.setQuestionNum(questionNum);
                qv.setQuestionScore(questionScore);
                ls.add(qv);
            }
            ll.put(questionType+"&"+difficulty2,ls);
            return ll;
        }
    }

    @ApiOperation(value = " 删除竞答试卷 ")
    @RequestMapping("/deletePaperRelById")
    public ApiResult deletePaperRelById(Long examId,Long paperId){
        QueryWrapper<CompetitionExamPaperRel> queryWrapper  =  new QueryWrapper<>();
        queryWrapper.eq("ac_id",examId);
        //queryWrapper.eq("paper_id",paperId);
        queryWrapper.orderByAsc("paper_sort");
        List<CompetitionExamPaperRel>  ll = competitionExamPaperRelService.list(queryWrapper);
        CompetitionExamPaperRel competitionExamPaperRel = ll.stream().filter(e -> e.getPaperId().equals(paperId)).findFirst().get();
        if(competitionExamPaperRel.getPaperSort()<ll.size()){
            for(int i = competitionExamPaperRel.getPaperSort();i<ll.size();i++){
                ll.get(i).setPaperSort(i);
            }
            competitionExamPaperRelService.saveOrUpdateBatch(ll);
        }
        competitionExamPaperRelService.removeById(competitionExamPaperRel.getId());


        return ApiResultHandler.buildApiResult(200, "删除试卷成功", null);
    }
}

