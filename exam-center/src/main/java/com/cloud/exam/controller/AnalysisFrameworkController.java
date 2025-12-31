package com.cloud.exam.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.core.ExamConstants;
import com.cloud.exam.model.exam.*;
import com.cloud.exam.service.*;
import com.cloud.exam.utils.exam.QuestionUtils;
import com.cloud.exam.vo.StudentAnswerVO;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.utils.PageUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by dyl on 2022/5/24.
 */
@RestController
public class AnalysisFrameworkController {

    @Autowired
    private AnalysisFrameworkService analysisFrameworkService ;
    @Autowired
    private QuestionService questionService ;
    @Autowired
    private DrawResultService drawResultService ;
    @Autowired
    private PaperService paperService ;
    @Autowired
    private StudentAnswerService studentAnswerService ;
    @Autowired
    private QuestionKpRelService questionKpRelService ;
    @Autowired
    private PaperManageRelService paperManageRelService ;


    /**
     * 查找模板列表
     */
    @RequestMapping(value = "getAnalysisFrameworkList",method = RequestMethod.GET)
    public IPage<AnalysisFramework> getAnalysisFrameworkList(@RequestParam Map<String, Object> params){
        int start = Integer.valueOf(params.get(PageUtil.START).toString());
        int size = Integer.valueOf(params.get(PageUtil.LENGTH).toString());
        IPage<AnalysisFramework> analysisFrameworkIPage = analysisFrameworkService.findAll(new Page<>((start / size) + 1, size), params);

        return analysisFrameworkIPage ;

    }

    /**
     * 根据id查找模板
     */
    @RequestMapping(value = "getAnalysisFrameworkByid/{id}",method = RequestMethod.GET)
    public ApiResult getAnalysisFrameworkByid(@PathVariable("id") Long id){

        AnalysisFramework analysisFramework = analysisFrameworkService.getById(id);

        return ApiResultHandler.buildApiResult(200, "获取成功", analysisFramework);

    }
    /**
     * 添加模板
     */
    @RequestMapping(value = "addAnalysisFramework",method = RequestMethod.POST)
    public ApiResult addAnalysisFramework(@RequestBody AnalysisFramework analysisFramework){
        analysisFramework.setCreateTime(new Date());
        analysisFrameworkService.save(analysisFramework);
        return ApiResultHandler.buildApiResult(200, "添加成功", null);

    }

    /**
     * 修改模板
     */
    @RequestMapping(value = "updateAnalysisFramework",method = RequestMethod.POST)
    public ApiResult updateAnalysisFramework(@RequestBody AnalysisFramework analysisFramework){
        if(ObjectUtil.isNotNull(analysisFramework.getId())){
            analysisFramework.setUpdateTime(new Date());
        }else {
            analysisFramework.setCreateTime(new Date());
        }
        analysisFrameworkService.saveOrUpdate(analysisFramework);
        return ApiResultHandler.buildApiResult(200, "修改成功", null);

    }

    /**
     * 删除模板
     */
    @RequestMapping(value = "delAnalysisFramework/{id}",method = RequestMethod.GET)
    public ApiResult delAnalysisFramework(@PathVariable("id") Long id){
        QueryWrapper<Question> questionQueryWrapper = new QueryWrapper<>();
        questionQueryWrapper.eq("model_id",id);
        List<Question> list = questionService.list(questionQueryWrapper);
        list.parallelStream().forEach(e->{
            e.setModelId(null);
            e.setModelUrl(null);
            questionService.saveOrUpdate(e);
        });
        analysisFrameworkService.removeById(id);
        return ApiResultHandler.buildApiResult(200, "删除成功", null);

    }

    /**
     * 情析和整编判卷
    @RequestMapping(value = "judgeAnalysisPaper",method = RequestMethod.POST)
    public ApiResult judgeAnalysisPaper(@RequestParam String identityId, @RequestBody List<StudentAnswerVO> list){
        QueryWrapper<DrawResult>  q = new QueryWrapper<>();
        q.eq("identity_card",identityId);
        DrawResult dr = drawResultService.getOne(q);
        Double totalScore = 0d ;
        for (StudentAnswerVO vo:list) {
            StudentAnswer byId = studentAnswerService.getById(vo.getId());
            totalScore += vo.getActualScore();
            byId.setJudgeRemark(vo.getJudgeRemark());
            byId.setPaperId(dr.getPaperId());
            byId.setUpdateTime(new Date());
            byId.setStudentId(dr.getUserId());
            byId.setKpScores(vo.getKpScores());
            JSONObject jsonObject = JSONObject.parseObject(vo.getKpScoreList()+"");
            byId.setKpScores(jsonObject.toJSONString());
            studentAnswerService.saveOrUpdate(byId);
        }
        dr.setScore(totalScore);
        dr.setJudgePerson(AppUserUtil.getLoginAppUser().getId());
        dr.setUserStatus(ExamConstants.EXAM_FINISH);
        drawResultService.saveOrUpdate(dr);
        return ApiResultHandler.buildApiResult(200, "获取成功", null);
    }

    *//**
     * 获取考生情析和整编的答案
     *//*
    @RequestMapping(value = "getAnalysisStudentAnswer",method = RequestMethod.GET)
    public ApiResult getAnalysisStudentAnswer(@RequestParam String identityId){
        QueryWrapper<DrawResult>  q = new QueryWrapper<>();
        q.eq("identity_card",identityId);
        DrawResult one = drawResultService.getOne(q);
        QueryWrapper<StudentAnswer>  queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id",one.getUserId());
        queryWrapper.eq("paper_id",one.getPaperId());
        List<StudentAnswer> list = studentAnswerService.list(queryWrapper);
        List<StudentAnswerVO> ll = new ArrayList<>();
        for (StudentAnswer stu:list) {
            StudentAnswerVO vo = new StudentAnswerVO();
            BeanUtils.copyProperties(stu, vo);
            Question byId = questionService.getById(vo.getQuestionId());
            HashMap<Long,String> map = QuestionUtils.getKpDetailsByQuestion(byId);
            vo.setKpDetails(map);
            vo.setQuestion(byId.getQuestion());
            JSONObject js = new JSONObject();
            js.put("text", "");
            js.put("url", "");
            QueryWrapper<PaperManageRel> qw = new QueryWrapper<>();
            qw.eq("paper_id", one.getPaperId());
            qw.eq("question_id", vo.getQuestionId());
            PaperManageRel paperManageRel = paperManageRelService.getOne(qw);
            vo.setKeywords(byId.getKeywords() == null ? js.toJSONString() : byId.getKeywords());
            vo.setAnalysis(byId.getAnalysis() == null ? js.toJSONString() : byId.getAnalysis());
            vo.setAnswer(byId.getAnswer() == null ? js.toJSONString() : byId.getAnswer());
            vo.setQuestionScore(paperManageRel.getScore());
            vo.setStuAnswers(stu.getStuAnswer());
            ll.add(vo) ;
        }
        return ApiResultHandler.buildApiResult(200, "获取成功", ll);
    }*/
}
