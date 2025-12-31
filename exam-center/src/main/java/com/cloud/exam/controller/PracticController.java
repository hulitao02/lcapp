package com.cloud.exam.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.core.ExamConstants;
import com.cloud.exam.model.exam.*;
import com.cloud.exam.service.*;
import com.cloud.exam.utils.Tools;
import com.cloud.exam.websocket.WebSocketServer;
import com.cloud.model.user.LoginAppUser;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.utils.DateConvertUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dyl on 2021/09/10.
 */
@RestController
public class PracticController {

    @Autowired
    private ExamService examService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private StudentAnswerService studentAnswerService;
    @Autowired
    private DrawResultService drawResultService;
    @Autowired
    private PaperService paperService;
    @Autowired
    private PaperManageRelService paperManageRelService;



    @ApiOperation(value = "考生点击考试，将考生实操考试试卷信息推送到第三方接口")
    @RequestMapping(value = "/sendQuestionDetailsById",method = RequestMethod.GET)
    public ApiResult sendQuestionDetailsById(String  identityCard){
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("identity_card",identityCard);
        DrawResult one = drawResultService.getOne(queryWrapper);
        Paper byId = paperService.getById(one.getPaperId());
        QueryWrapper<PaperManageRel> qw = new QueryWrapper<>();
        qw.eq("paper_id",one.getPaperId());
        List<PaperManageRel> list = paperManageRelService.list(qw);
        List ll = new ArrayList();
        for (PaperManageRel pml:list) {
            List ls = new ArrayList();
            ls.add(pml.getQuestionId());
            ls.add(pml.getScore());
            ll.add(ls);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code",ll);
        jsonObject.put("userid",loginAppUser.getId());
        jsonObject.put("username",loginAppUser.getNickname());
        jsonObject.put("usertype",0);
        jsonObject.put("examid",one.getAcId());
        jsonObject.put("paperid",one.getPaperId());
        Exam exam = examService.getById(one.getAcId());
        jsonObject.put("endtime",exam.getEndTime()==null?"":exam.getEndTime());

        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "成功",jsonObject.toJSONString());
    }


    @ApiOperation(value = "接收考试信息进行保存考试结果")
    @RequestMapping(value = "/saveQuestionScoreById",method = RequestMethod.GET)
    public ApiResult saveQuestionScoreById(String text){
        try{
            JSONObject  jsonObject = JSONObject.parseObject(text);
            Object userid = jsonObject.get("userid");
            Object examid = jsonObject.get("examid");
            Object usertype = jsonObject.get("usertype");
            Object paperid = jsonObject.get("paperid");
            Paper paper = paperService.getById(Long.valueOf(String.valueOf(paperid)));
            String endtime =(String)jsonObject.get("endtime");
            Exam exam = examService.getById(Long.valueOf(String.valueOf(examid)));
            Date startTime = exam.getStartTime();
            Date date = DateConvertUtils.StringTime2Data(endtime);
            String costTime = DateConvertUtils.longTimeToDay(startTime, date);
            List<JSONArray> code = (List<JSONArray>)jsonObject.get("code");
            double score = 0;
            List<JSONObject> ll = new ArrayList<>();
            for (JSONArray jsonArray:code) {
                StudentAnswer sa = new StudentAnswer();
                sa.setQuestionId(Long.valueOf(String.valueOf(jsonArray.get(0))));
                Question q = questionService.getById(Long.valueOf(String.valueOf(jsonArray.get(0))));
                sa.setActualScore(Double.valueOf(String.valueOf(jsonArray.get(1))));
                JSONObject jsonObject1 = new JSONObject(true);
                jsonObject1.put("name",q.getQuestion());
                jsonObject1.put("score",Double.valueOf(String.valueOf(jsonArray.get(1))));
                ll.add(jsonObject1);
                score+=Double.valueOf(String.valueOf(jsonArray.get(1)));
                sa.setStudentId(Long.valueOf(String.valueOf(userid)));
                sa.setPaperId(Long.valueOf(String.valueOf(paperid)));
                sa.setType(8);
                sa.setPaperType(ExamConstants.PAPER_SHICAO);
                sa.setAcId(exam.getId());
                sa.setPdType(q.getPdType());
                studentAnswerService.save(sa);
            }
            QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("paper_id",paperid);
            queryWrapper.eq("user_id",userid);
            queryWrapper.eq("ac_id",examid);
            DrawResult one = drawResultService.getOne(queryWrapper);
            one.setScore(score);
            one.setCostTime(costTime);
            one.setUserStatus(ExamConstants.EXAM_FINISH);
            drawResultService.saveOrUpdate(one);
            JSONObject js = new JSONObject();
            js.put("totalScore",score);
            js.put("question",ll);
            if(ExamConstants.PAPER_SHICAO.equals(paper.getType())){
                WebSocketServer.sendInfo("dzs",userid+"");
                Tools.putJudgeCache("dzs"+one.getIdentityCard(),js.toJSONString());
            }
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "保存成功",null);
        }catch (Exception e){
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "保存失败",null);
        }
    }

    @RequestMapping(value = "/getUserExamDetailsById",method = RequestMethod.GET)
    public ApiResult getUserExamDetailsById(String identityId){
        Object requestCache = Tools.getJudgeCache("dzs" + identityId);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "",requestCache);
    }
}
