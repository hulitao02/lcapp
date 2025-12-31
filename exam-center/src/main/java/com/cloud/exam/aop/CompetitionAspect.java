package com.cloud.exam.aop;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ExamConstants;
import com.cloud.exam.model.exam.*;
import com.cloud.exam.service.*;
import com.cloud.exam.utils.Tools;
import com.cloud.exam.vo.DrawResultVO;
import com.cloud.exam.vo.QuestionVO;
import com.cloud.exam.vo.StudentAnswerVO;
import com.cloud.exam.websocket.WebSocketServer;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.utils.RedisUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dyl on 2021/06/30.
 * 竞答活动流程处理
 */
@Component
@Aspect
public class CompetitionAspect {

    @Autowired
    private DrawResultService drawResultService;
    @Autowired
    private ExamService examService;
    @Autowired
    private PaperService paperService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private PaperManageRelService paperManageRelService;
    @Autowired
    private CompetitionExamPaperRelService competitionExamPaperRelService;

    //主持人点击下一题时需要指定的组进行答题，被指定答题的组需要展示试题列表
    @Around("execution(* com.cloud.exam.controller.CompetitionQuestionController.changeAnswerGroup(..) )")
    public Object finishQuestionByhost(ProceedingJoinPoint pjp) throws Throwable{
        //执行启动考试过程
        Object proceed = pjp.proceed();
        ApiResult apiResult = (ApiResult) proceed;
        //参数值
        Object[] args = pjp.getArgs();
        Long examId = (Long) args[0];
        Long paperId = (Long) args[1];
        Paper paper = paperService.getById(paperId);
        Exam exam = examService.getById(examId);
        String placeNum = (String) args[2];
        List<DrawResult> ll = new ArrayList<>();
        if(apiResult.getCode()==200){
            if(paper.getType()==5){
                //抢答考试  主持人选择了答题小组  需要向各其他小组展示答题小组
                QueryWrapper queryWrapper = new QueryWrapper();
                queryWrapper.eq("ac_id",examId);
                queryWrapper.eq("paper_id",paperId);
                queryWrapper.ne("place_num",placeNum);
                List<DrawResult>  ls = drawResultService.list(queryWrapper);
                List<Long>  lsi = new ArrayList<>();
                ls.stream().forEach(e->lsi.add(e.getUserId()));
                lsi.add(exam.getHostId());
                QueryWrapper qw = new QueryWrapper();
                qw.eq("user_id", apiResult.getData());
                qw.eq("ac_id", examId);
                DrawResult byId = drawResultService.getOne(qw);
                DrawResultVO v = new DrawResultVO();
                BeanUtils.copyProperties(byId, v);
                for(Long userId : lsi){
                    //v.setGroupName(ChangeInt2chn.tranInt(Integer.valueOf(byId.getPlaceNum())));
                    WebSocketServer.sendInfo(JSONObject.toJSONString(v), userId+ "");
                }
            }else{
                List<QuestionVO> data = (List<QuestionVO>)apiResult.getData();
                List<QuestionVO>  list =   (List<QuestionVO>)apiResult.getData();
                QueryWrapper queryWrapper = new QueryWrapper();
                queryWrapper.eq("ac_id",examId);
                //queryWrapper.eq("paper_id",paperId);
                queryWrapper.eq("place_num",placeNum);
                List<DrawResult> ls = drawResultService.list(queryWrapper);
                for (DrawResult dr:ls) {
                    WebSocketServer.sendInfo(JSONObject.toJSONString(data),dr.getUserId()+"");
                }
            }

        }
        return proceed;
    }

    //主持人点击开始答题，需要给将要答题的的人发送试题消息
    @Around("execution(* com.cloud.exam.controller.CompetitionQuestionController.startQuestionByhost(..) )")
        public Object startQuestionByhost(ProceedingJoinPoint pjp) throws Throwable{
        //执行启动考试过程
        Object proceed = pjp.proceed();
        ApiResult apiResult = (ApiResult) proceed;
        //参数值
        Object[] args = pjp.getArgs();
        Long examId = Long.valueOf(args[0]+"");
        QueryWrapper<DrawResult> q = new QueryWrapper<>();
        q.eq("ac_id",examId);
        List<DrawResult> ll = drawResultService.list(q);
        Exam exam = examService.getById(examId);
        Long paperId = Long.valueOf(args[1]+"");
        Paper paper = paperService.getById(paperId);
        List<Object> objects = redisUtils.lGet("competition:" + examId + ":" + paperId, 0, -1);
        List<QuestionVO> list = (List<QuestionVO>)objects.get(0);
        //当前试题的下标
        Integer qIndex = Integer.valueOf(args[2]+"");
        if(apiResult.getCode()==200){
            HashMap<Object,Object> data = (HashMap<Object,Object>) apiResult.getData();
            Object paperType = data.get("paperType");
            List<Long> ls = new ArrayList<>();
            List<DrawResult> drawList = (List<DrawResult>)data.get("drawList");
            QuestionVO questionVO = new QuestionVO();
            questionVO.setPaperId(paperId);
            questionVO.setExamId(examId);
            questionVO.setQuestionIndex(qIndex);
            questionVO.setQuestionNum(list.size());
            if(paper.getType().equals(ExamConstants.PAPER_XUANDA) || paper.getType().equals(ExamConstants.PAPER_QIANGDA)){
                questionVO.setOnly("start");
            }
            questionVO.setPaperType(Integer.parseInt(paperType.toString()));
            if(Integer.parseInt(paperType.toString())==4){
                questionVO.setQuestionTime(list.get(qIndex).getQuestionTime());
            }
            //drawList 当前答题人员
            for (DrawResult dr:drawList) {
                //答题人员id
                ls.add(dr.getUserId());
                if(paper.getType().equals(ExamConstants.PAPER_QIANGDA)){
                    questionVO.setGroupName("");
                }else {
                    questionVO.setGroupName(Tools.getCompetitionGroupCache(examId+"&"+dr.getPlaceNum()));
                }
                questionVO.setTotalScore(Tools.getCompetitionScoreCache(examId+"&"+dr.getPlaceNum()));
            }

            if(!paper.getType().equals(ExamConstants.PAPER_QIANGDA)){
                questionVO.setUserIds(ls);
            }
            for (DrawResult dr:ll) {
                questionVO.setTotalScore(Tools.getCompetitionScoreCache(examId+"&"+dr.getPlaceNum()));
                String s = JSONObject.toJSONString(questionVO);
                WebSocketServer.sendInfo(s,dr.getUserId()+"");
            }
                questionVO.setQuestionIndex(qIndex);
                questionVO.setQuestionNum(list.size());
            if(ExamConstants.PAPER_QIANGDA.equals(paperType)){
               /* JSONObject jsonObject = new JSONObject();
                jsonObject.put("questionIndex",qIndex);
                jsonObject.put("questionNum",list.size());
                WebSocketServer.sendInfo(jsonObject.toJSONString(),exam.getHostId()+"");*/
                WebSocketServer.sendInfo(JSONObject.toJSONString(questionVO),exam.getHostId()+"");
            } else {
                questionVO.setTotalScore(null);
                WebSocketServer.sendInfo(JSONObject.toJSONString(questionVO),exam.getHostId()+"");
            }
        }
         return proceed;
    }

    //考生提交答案后向主持人推送考生答案进行大屏展示
    @Around("execution(* com.cloud.exam.controller.CompetitionQuestionController.saveStudentQuestionAnswer(..) )")
    public Object saveStudentAnswer(ProceedingJoinPoint pjp) throws Throwable{
        //执行启动考试过程
        Object proceed = pjp.proceed();
        ApiResult apiResult = (ApiResult) proceed;
        //参数值
        Object[] args = pjp.getArgs();
        StudentAnswerVO sv = (StudentAnswerVO) args[0];
        Long examId = sv.getExamId();
        Long hostId = examService.getById(examId).getHostId();
        if(apiResult.getCode()==200){
            Object stuAnswer = sv.getStuAnswers();
            String s = JSONObject.toJSONString(stuAnswer);
            JSONObject  js = new JSONObject();
            js.put("type","1");
            js.put("answer",stuAnswer);
            js.put("only","student");
            QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ac_id",examId);
            queryWrapper.ne("user_id", AppUserUtil.getLoginAppUser().getId());
            List<DrawResult> list = drawResultService.list(queryWrapper);
            list.stream().forEach(e->{
                try {
                    WebSocketServer.sendInfo(js.toJSONString(),e.getUserId()+"");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
            WebSocketServer.sendInfo(js.toJSONString(),hostId+"");
        }
        return proceed;
    }

    /**
     * 选答考试中 考生选择试题需要将信息推送给主持人
     */
    @Around("execution(* com.cloud.exam.controller.CompetitionQuestionController.studentChooseQuestion(..) )")
    public Object sendQuestionDetails(ProceedingJoinPoint pjp) throws Throwable {
        //执行启动考试过程
        Object proceed = pjp.proceed();
        ApiResult apiResult = (ApiResult) proceed;
        //参数值
        Object[] args = pjp.getArgs();
        Long examId = (Long)args[0];
        Long paperId = (Long)args[1];
        Long questionId = (Long)args[2];
        String placeNum = (String)args[4];
        Paper paper = paperService.getById(paperId);
        List<Long> list = new ArrayList<>();

       /* List<Object> objects = redisUtils.lGet("competition:" + examId + ":" + paperId, 0, -1);
        List<QuestionVO> list = (List<QuestionVO>)objects.get(0);
        int index = 0;
        for(int i =0 ;i<list.size();i++){
            if(list.get(i).getId().equals(questionId)){
                index  = i;
                break;
            }
        }*/

        QueryWrapper<PaperManageRel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("paper_id",paperId);
        queryWrapper.eq("question_id",questionId);
        PaperManageRel paperManageRel = paperManageRelService.getOne(queryWrapper);
        QueryWrapper<CompetitionExamPaperRel> qw = new QueryWrapper<>();
        qw.eq("paper_id",paperId);
        qw.eq("ac_id",examId);
        CompetitionExamPaperRel competitionExamPaperRel = competitionExamPaperRelService.getOne(qw);
        Question q = questionService.getById(questionId);
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(q,questionVO);

        questionVO.setExamId(examId);
        questionVO.setPaperId(paperId);
        //questionVO.setQuestionIndex(index);
        questionVO.setQuestionTime(paperManageRel.getQuestionTime());
        questionVO.setPaperType(competitionExamPaperRel.getPaperType());
        questionVO.setOnly("host");
        questionVO.setUserId(AppUserUtil.getLoginAppUser().getId());
        if(apiResult.getCode()==200){
            Map<String,Object> map =(Map<String,Object>)apiResult.getData();
            Integer index = (Integer) map.get("index");
            questionVO.setQuestionIndex(index);
            if(ExamConstants.PAPER_QIANGDA.equals(paper.getType())){
                QueryWrapper<DrawResult> qq = new QueryWrapper<>();
                qq.eq("ac_id",examId);
                qq.eq("place_num",placeNum);
                List<DrawResult> ll = drawResultService.list(qq);
                ll.stream().forEach(e->list.add(e.getUserId()));
                questionVO.setUserIds(list);
            }
            List<Long>  userIds =(List<Long> ) map.get("userIds");
            for (Long userId:userIds){
                WebSocketServer.sendInfo(JSONObject.toJSONString(questionVO),userId+"");
            }
        }
        return proceed;
    }


    //考生选择试题难度后向主持人推送考生答案进行大屏展示
    @Around("execution(* com.cloud.exam.controller.CompetitionQuestionController.chooseQuestionDifficty(..) )")
    public Object chooseQuestionDifficty(ProceedingJoinPoint pjp) throws Throwable{
        Object proceed = pjp.proceed();
        ApiResult apiResult = (ApiResult) proceed;
        //参数值
        Object[] args = pjp.getArgs();
        Long examId = (Long) args[0];
        QueryWrapper<DrawResult> drawResultQueryWrapper = new QueryWrapper<>();
        drawResultQueryWrapper.eq("ac_id",examId);
        List<DrawResult> list = drawResultService.list(drawResultQueryWrapper);
        String difficuty = (String) args[1];
        Long userId = (Long) args[2];
        JSONObject jsonObject = new JSONObject();
        if(apiResult.getCode()==200){
            jsonObject.put("only","sync");
            jsonObject.put("key",difficuty);
            jsonObject.put("userId",userId);
            list.stream().forEach(e->{
                try {
                    WebSocketServer.sendInfo(jsonObject.toJSONString(),e.getUserId()+"");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
            WebSocketServer.sendInfo(jsonObject.toJSONString(),apiResult.getData()+"");
        }
        return proceed;
    }

    //考生选择答案后向主持人推送考生答案进行大屏展示
    @Around("execution(* com.cloud.exam.controller.CompetitionQuestionController.sendStuAnswers2host(..) )")
    public Object sendStuAnswers2host(ProceedingJoinPoint pjp) throws Throwable{
        Object proceed = pjp.proceed();
        ApiResult apiResult = (ApiResult) proceed;
        //参数值
        Object[] args = pjp.getArgs();
        Map<Object,Object> ans = (Map<Object,Object>)args[0];
        JSONObject jsonObject = new JSONObject();
        if(apiResult.getCode()==200){
            Long examId =Long.valueOf(String.valueOf(apiResult.getData()));
            Exam exam = examService.getById(examId);
            QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ac_id",examId);
            List<DrawResult> list = drawResultService.list(queryWrapper);
            jsonObject.put("only","ans");
            jsonObject.put("ans",ans.get("ans"));
            list.stream().forEach(e->{
                try {
                    WebSocketServer.sendInfo(jsonObject.toJSONString(),e.getUserId()+"");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
            WebSocketServer.sendInfo(jsonObject.toJSONString(),exam.getHostId()+"");
        }
        return proceed;
    }

    //主持人修改分数后给学员和主持人发送消息
    @Around("execution(* com.cloud.exam.controller.CompetitionQuestionController.editGroupScore(..) )")
    public Object editGroupScore(ProceedingJoinPoint pjp) throws Throwable{
        Object proceed = pjp.proceed();
        ApiResult apiResult = (ApiResult) proceed;
        //参数值
        Object[] args = pjp.getArgs();
        Map<Object,Object> ans = (Map<Object,Object>)args[0];
        JSONObject jsonObject = new JSONObject();
        if(apiResult.getCode()==200){
            Double score =Double.valueOf(String.valueOf(apiResult.getData()));
            Exam exam = examService.getById(Long.valueOf(String.valueOf(ans.get("examId"))));
            String placeNum = String.valueOf(ans.get("placeNum"));
            QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ac_id",Long.valueOf(String.valueOf(ans.get("examId"))));
            queryWrapper.eq("place_num",placeNum);
            List<DrawResult> list = drawResultService.list(queryWrapper);
            jsonObject.put("totalScore",score);
            jsonObject.put("only","score");
            list.stream().forEach(e->{
                try {
                    WebSocketServer.sendInfo(jsonObject.toJSONString(),e.getUserId()+"");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
            WebSocketServer.sendInfo(jsonObject.toJSONString(),exam.getHostId()+"");
        }
        return proceed;
    }

    //主持人点击结束活动向学员和主持人发送消息
    @Around("execution(* com.cloud.exam.controller.CompetitionQuestionController.finishExamByhost(..) )")
    public Object finishExamByhost(ProceedingJoinPoint pjp) throws Throwable{
        Object proceed = pjp.proceed();
        ApiResult apiResult = (ApiResult) proceed;
        //参数值
        Object[] args = pjp.getArgs();
        Object arg = args[0];
        JSONObject jsonObject = new JSONObject();
        if(apiResult.getCode()==200){
            Exam exam = examService.getById(Long.valueOf(String.valueOf(arg)));
            QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ac_id",Long.valueOf(String.valueOf(arg)));
            List<DrawResult> list = drawResultService.list(queryWrapper);
            jsonObject.put("only","end");
            list.stream().forEach(e->{
                try {
                    WebSocketServer.sendInfo(jsonObject.toJSONString(),e.getUserId()+"");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
            WebSocketServer.sendInfo(jsonObject.toJSONString(),exam.getHostId()+"");
        }
        return proceed;
    }

}
