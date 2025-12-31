package com.cloud.exam.aop;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ExamConstants;
import com.cloud.exam.dao.DrawResultDao;
import com.cloud.exam.model.exam.*;
import com.cloud.exam.service.*;
import com.cloud.exam.utils.Tools;
import com.cloud.exam.vo.DrawResultVO;
import com.cloud.exam.vo.QuestionVO;
import com.cloud.exam.vo.StudentAnswerVO;
import com.cloud.exam.websocket.WebSocketServer;
import com.cloud.utils.RedisUtils;
import com.cloud.utils.Validator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抢答试卷处理
 * Created by dyl on 2021/07/09.
 */
@Component
@Aspect
public class RaceQuestionAspect {

    @Autowired
    private ExamService examService;
    @Autowired
    private StudentAnswerService studentAnswerService;
    @Autowired
    private DrawResultService drawResultService;
    @Autowired
    private PaperService paperService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private PaperManageRelService paperManageRelService;
    @Autowired
    DrawResultDao drawResultDao;

    //向主持人展示获取答题权的小组名称
    @Around("execution(* com.cloud.exam.controller.RaceQuestionController.getQuestionAcessKey(..) )")
    public Object getQuestionAcessKey(ProceedingJoinPoint pjp) throws Throwable {
        //执行启动考试过程
        Object proceed = pjp.proceed();
        //参数值
        Object[] args = pjp.getArgs();
        ApiResult apiResult = (ApiResult) proceed;
        Exam exam = examService.getById((Long) args[0]);
        Paper paper = paperService.getById((Long) args[1]);
        QueryWrapper<PaperManageRel> qwq = new QueryWrapper<>();
        qwq.eq("paper_id", (Long) args[1]);
        qwq.eq("question_id", (Long) args[3]);
        PaperManageRel list1 = paperManageRelService.list(qwq).get(0);

        QueryWrapper qw = new QueryWrapper();
        qw.eq("ac_id", (Long) args[0]);
        List<DrawResult> list = drawResultService.list(qw);
        if (apiResult.getCode() == 200) {
            long userId = (Long) args[2];
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("user_id", userId);
            queryWrapper.eq("ac_id", (Long) args[0]);
            DrawResult byId = drawResultService.getOne(queryWrapper);
            DrawResultVO v = new DrawResultVO();
            BeanUtils.copyProperties(byId, v);
            if (!Validator.isEmpty(list1)) {
                v.setQuestionTime(list1.getQuestionTime());
            }
            v.setPaperType(paper.getType());
            //v.setGroupName(ChangeInt2chn.tranInt(Integer.valueOf(byId.getPlaceNum())));
            v.setGroupName(Tools.getCompetitionGroupCache(args[0] + "&" + byId.getPlaceNum()));
            WebSocketServer.sendInfo(JSONObject.toJSONString(v), exam.getHostId() + "");
            for (DrawResult dr : list) {
                WebSocketServer.sendInfo(JSONObject.toJSONString(v), dr.getUserId() + "");
            }
        }
        return proceed;
    }

    //主持人判题向考生推送试题答案
    @Around("execution(* com.cloud.exam.controller.CompetitionQuestionController.judgeStudentAnswer(..) )")
    public Object judgeStudentAnswer(ProceedingJoinPoint pjp) throws Throwable {
        //执行启动考试过程
        Object proceed = pjp.proceed();
        //参数值
        Object[] args = pjp.getArgs();
        HashMap<String, Object> map = (HashMap<String, Object>) args[0];
        Long examId = Long.valueOf(String.valueOf(map.get("examId")));
        Long paperId = Long.valueOf(String.valueOf(map.get("paperId")));
        Integer qIndex = Integer.valueOf(String.valueOf(map.get("qIndex")));

        ApiResult apiResult = (ApiResult) proceed;
        if (apiResult.getCode() == 200) {
            Paper paper = paperService.getById(paperId);
            Exam exam = examService.getById(examId);
            List<Object> objects = redisUtils.lGet("competition:" + examId + ":" + paperId, 0, -1);
            List<QuestionVO> o = (List<QuestionVO>) objects.get(0);
            QueryWrapper<StudentAnswer> queryWrapper = new QueryWrapper();
            queryWrapper.eq("question_id", o.get(qIndex).getId());
            queryWrapper.eq("paper_id", paperId);
            List<StudentAnswer> list = studentAnswerService.list(queryWrapper);
            StudentAnswerVO studentAnswerVO = new StudentAnswerVO();
            QueryWrapper<DrawResult> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("ac_id", examId);
            List<DrawResult> ll = drawResultService.list(queryWrapper1);
            if (list.size() > 0) {
                StudentAnswer studentAnswer = list.get(0);
                BeanUtils.copyProperties(studentAnswer, studentAnswerVO);
                //Long userId = studentAnswer.getStudentId();
                if (!Validator.isEmpty(studentAnswer.getActualScore()) && studentAnswer.getActualScore() > 0) {
                    studentAnswerVO.setAnswer(studentAnswer.getStuAnswer());
                    studentAnswerVO.setFlag(true);
                } else {
                    JSONObject jsonObject = JSONObject.parseObject(o.get(qIndex).getAnswer());
                    String s = jsonObject.getString("text");
                    studentAnswerVO.setAnswer(s);
                    studentAnswerVO.setFlag(false);
                }

            } else {
                studentAnswerVO.setAnswer("");
                studentAnswerVO.setFlag(false);
            }
            for (DrawResult dr : ll) {
                //Double competitionScoreCache = Tools.getCompetitionScoreCache(examId + "&" + dr.getPlaceNum());
                studentAnswerVO.setTotalScore(dr.getScore());
                WebSocketServer.sendInfo(JSONObject.toJSONString(studentAnswerVO), dr.getUserId() + "");
            }
            if (paper.getType().equals(ExamConstants.PAPER_QIANGDA)) {
                Object o1 = redisUtils.get("competition:race:" + examId + ":" + o.get(qIndex).getId());
                DrawResult dr = ll.stream().filter(e -> e.getUserId().equals(o1)).findAny().orElse(null);
                studentAnswerVO.setPlaceNum(dr == null ? "" : dr.getPlaceNum());
            }
            WebSocketServer.sendInfo(JSONObject.toJSONString(studentAnswerVO), exam.getHostId() + "");
        }

        return proceed;
    }

    //抢答考试中点击开始答题后向每个考生发送所有参与考试的人员id
    @Around("execution(* com.cloud.exam.controller.RaceQuestionController.getUserIds2host(..) )")
    public Object getUserIds2host(ProceedingJoinPoint pjp) throws Throwable {
        //执行启动考试过程
        Object proceed = pjp.proceed();
        ApiResult apiResult = (ApiResult) proceed;
        //参数值
        Object[] args = pjp.getArgs();
        Long examId = Long.valueOf(args[0] + "");
        Exam exam = examService.getById(examId);
        if (apiResult.getCode() == 200) {
            List<Long> data = (List<Long>) apiResult.getData();
            QuestionVO questionVO = new QuestionVO();
            questionVO.setPaperType(ExamConstants.PAPER_QIANGDA);
            questionVO.setUserIds(data);
            questionVO.setOnly("race");
            String s = JSONObject.toJSONString(questionVO);
            for (Long userId : data) {
                WebSocketServer.sendInfo(s, userId + "");
            }
            WebSocketServer.sendInfo(s, exam.getHostId() + "");
        }
        return proceed;
    }


    /**
     * @author:胡立涛
     * @description: TODO 查看学员端桌面请求（web端发送api接口时，向webstock 发送信息）
     * @date: 2022/11/7
     * @param: [pjp]
     * @return: java.lang.Object
     */
    @Around("execution(* com.cloud.exam.controller.ExamController.jpWbe(..) )")
    public Object jpWbe(ProceedingJoinPoint pjp) throws Throwable {
        Object proceed = pjp.proceed();
        ApiResult apiResult = (ApiResult) proceed;
        Object[] args = pjp.getArgs();
        Map<String, Object> map = (Map<String, Object>) args[0];
        Long examId = map.get("examId") == null ? null : Long.valueOf(map.get("examId").toString());
        Long studentId = map.get("studentId") == null ? null : Long.valueOf(map.get("studentId").toString());
        Long teacherId = map.get("teacherId") == null ? null : Long.valueOf(map.get("teacherId").toString());
        String base64 = map.get("base64") == null ? null : map.get("base64").toString();
        if (apiResult.getCode() == 200) {
            // 向监考端发送websockert消息
            if (base64 != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("studentId", studentId.toString());
                data.put("teacherId", teacherId.toString());
                data.put("base64", base64);
                data.put("examId",examId.toString());
                WebSocketServer.sendInfo(JSONObject.toJSONString(data), teacherId.toString());
            } else {
                QueryWrapper queryWrapper = new QueryWrapper();
                // 考试中
                queryWrapper.eq("user_status", 6);
                queryWrapper.eq("ac_id", examId);
                List<DrawResult> list = drawResultDao.selectList(queryWrapper);
                for (DrawResult dr : list) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("studentId", studentId.toString());
                    data.put("examId",examId.toString());
                    data.put("teacherId",teacherId);
                    WebSocketServer.sendInfo(JSONObject.toJSONString(data), dr.getUserId().toString());
                }
            }
        }
        return proceed;
    }
}
