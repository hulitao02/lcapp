package com.cloud.exam.aop;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ExamConstants;
import com.cloud.exam.annotation.UserActionAnnotation;
import com.cloud.exam.model.exam.*;
import com.cloud.exam.service.*;
import com.cloud.exam.utils.UserMessageUtils;
import com.cloud.exam.vo.DrawResultVO;
import com.cloud.exam.websocket.WebSocketServer;
import com.cloud.utils.ObjectUtils;
import com.cloud.utils.Validator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by dyl on 2021/04/20.
 * 拦截指定方法 用于消息保存发送
 */
@Component
@Aspect
public class UserActAspect {
    @Autowired
    private DrawResultService drawResultService;
    @Autowired
    private ExamService examService;
    @Autowired
    private UserActivityMessageService userActivityMessageService;
    @Autowired
    private PaperService paperService;
    @Autowired
    private ExamPlaceService examPlaceService;

    /**
     * 切点
     */
    @Pointcut("@annotation(com.cloud.exam.annotation.UserActionAnnotation)")
    public void pointCut(){

    }


    /**
     * 启动考试活动,保存发送消息
     */
    @Around("execution(* com.cloud.exam.controller.ExamController.getDrawResult(..) )")
    public Object launchExam(ProceedingJoinPoint pjp) throws Throwable {
        //执行启动考试过程
        Object proceed = pjp.proceed();
        ApiResult apiResult = (ApiResult) proceed;
        if(apiResult.getCode()==200){
            MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
            //参数名数组
            String[] parameters =  methodSignature.getParameterNames();
            //参数值
            Object[] args = pjp.getArgs();
            LinkedHashMap<String,Long> examId = (LinkedHashMap)args[0];
            Exam exam = examService.getById(examId.get("examId"));
            QueryWrapper qw = new QueryWrapper();
            qw.eq("ac_id", examId);
            //if(exam.getExamStatus().equals(ExamConstants.ACTIVITY_LAUNCH)){
            createMessage(exam,exam, ExamConstants.ACTIVITY_LAUNCH+"","");
            //}
        }
        return proceed;
    }
    /**
     * 根据更改的活动状态 （5-取消，4，判卷完成，活动完成，3考试结束 6 开始考试）
     * @param pjp
     * @throws Throwable
     */
    @Around("execution(* com.cloud.exam.controller.ExamController.updateExamStatus(..) )")
    public Object conCellExam(ProceedingJoinPoint pjp) throws Throwable {
        //执行启动考试过程
        Object proceed = pjp.proceed();
        ApiResult apiResult = (ApiResult) proceed;
     /*   MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        //参数名数组
        String[] parameters =  methodSignature.getParameterNames();*/
        if(apiResult.getCode()==200){
            //参数值
            Object[] args = pjp.getArgs();
            Long  examId = (Long) args[0];
            Exam exam = examService.getById(examId);
            Integer status = (Integer) args[1];
            QueryWrapper<DrawResult>  qw = new QueryWrapper<>();
            qw.eq("ac_id",examId);
            List<DrawResult> list = drawResultService.list(qw);
            if(status==5 ||  status==6){
                for (DrawResult dr:list) {
                    if(status==6){
                        dr.setLoginDate(new Date());
                        drawResultService.saveOrUpdate(dr);
                    }
                    createMessage(exam,dr,status+"","");
                }
            }else if(status==4){
                for (DrawResult dr:list) {
                    String paperName = paperService.getById(dr.getPaperId()).getPaperName();
                    createMessage(exam,dr,status+"",paperName);
                }
            }
        }
        return proceed;
    }

    /**
     * 学员登录考试端显示倒计时
     *
     * @throws IOException
     */
    @Around("execution(* com.cloud.exam.controller.ActivityFlowController.getExamDetails(..) )")
    public Object getActivityExamDetails(ProceedingJoinPoint pjp) throws Throwable {
        //执行主过程
        Object proceed = pjp.proceed();
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        //参数名数组
        String[] parameters =  methodSignature.getParameterNames();
        //参数值
        Object[] args = pjp.getArgs();
        String  identityId = (String) args[0];
        QueryWrapper qw = new QueryWrapper();
        qw.eq("identity_card", identityId);
        DrawResult one = drawResultService.getOne(qw);
        Paper paper =paperService.getById(one.getPaperId());
        Exam byId = examService.getById(one.getAcId());
        Integer isFix = byId.getIsFix();//1严格0自由
        Date endTime = byId.getEndTime();
        Date startTime = byId.getStartTime();
        Date loginDate = one.getLoginDate();
        Long extTime = 0L ;
        Integer status = byId.getExamStatus();
        if(!Validator.isEmpty(paper)){
            if(status.equals(ExamConstants.ACTIVITY_EXAM_START)){
                //考试已经开始
                if(!Validator.isEmpty(loginDate)){


                    //学员中途退出后 继续登录考试
                    Long time = System.currentTimeMillis()-loginDate.getTime();//上次登录距离现在的时间
                    if(isFix==0){
                        //自由考试
                        long l = paper.getTotalTime() * 60 * 1000 - time;//剩余试卷考试时间
                        long l1 = endTime.getTime() - System.currentTimeMillis();//距离活动结束时间的时长
                        if(l>l1){
                            //计算剩余时间
                            extTime = l1/1000;
                        }else {
                            extTime = l/1000;
                        }
                    }else{
                        //固定考试
                        extTime  = (paper.getTotalTime()*60*1000-time)/1000;
                    }
                    WebSocketServer.sendInfo(ExamConstants.MESSAGE_KAOSHI+String.valueOf(status)+"&"+extTime+"",one.getUserId()+"");
                }else{
                    //学员首次进入考试
                    if(isFix==0){
                        //自由考试
                        if(System.currentTimeMillis()+paper.getTotalTime()*60*1000>endTime.getTime()){
                            //计算剩余时间
                            extTime = endTime.getTime()- System.currentTimeMillis();
                        }else {
                            extTime  = paper.getTotalTime()*60L;
                        }
                    }else{
                        //固定考试,考试已经开始
                        extTime  = (paper.getTotalTime()*60L*1000-(System.currentTimeMillis()-startTime.getTime()))/1000;
                    }
                    WebSocketServer.sendInfo(ExamConstants.MESSAGE_KAOSHI+String.valueOf(status)+"&"+extTime+"",one.getUserId()+"");
                }
            }else{
                //考试还未开始
                //自由考试活动
                if(isFix==0){
                    if(paper.getTotalTime()*60*1000>(endTime.getTime()- startTime.getTime())){
                        //试卷时间超出活动结束时间
                        extTime = (endTime.getTime()- startTime.getTime())/1000;
                    }else{
                        extTime = paper.getTotalTime()*60L;
                    }

                }else{
                    //固定考试
                    extTime  = paper.getTotalTime()*60L;
                }
                //extTime  = paper.getTotalTime()*60L;
                WebSocketServer.sendInfo(ExamConstants.MESSAGE_KAOSHI+String.valueOf(status)+"&"+extTime+"",one.getUserId()+"");
            }
        }
        return proceed;
    }

    //将考生登录信息同步给监考页面
    @Around("pointCut()&&@annotation(userActionAnnotation)")
    public Object aspectUserAction(ProceedingJoinPoint pjp, UserActionAnnotation userActionAnnotation) throws Throwable {
        Object proceed = pjp.proceed();
        ApiResult  apiResult =  (ApiResult)proceed;
        String identityCard = "";
        //参数值
        Object[] args = pjp.getArgs();
        if(apiResult.getCode() == 200){
           /*if(userActionAnnotation.ActionType()==ExamConstants.EXAM_YES_LOGIN){
                //考生登录
               identityCard = (String)args[1];
           }
           if(userActionAnnotation.ActionType()==ExamConstants.EXAM_START || userActionAnnotation.ActionType()==ExamConstants.EXAM_WAIT_JUDGE){
                //考试开始
                 identityCard = (String)args[0];
            }*/
            identityCard = (String)args[0];
            QueryWrapper<DrawResult> qw = new QueryWrapper();
            qw.eq("identity_card",identityCard);
            DrawResult one = drawResultService.getOne(qw);
            Long userId1 = one.getUserId();
            Set<Long> minitorUserIds = getMinitorUserIds(identityCard);
            minitorUserIds.add(1L);
            for(Long  userId:minitorUserIds){
                WebSocketServer.sendInfo(ExamConstants.MESSAGE_JIANKAO+userActionAnnotation.ActionType()+"&"+userId1,userId+"");
            }
        }
        return apiResult;
    }

    /**
     * 监考页面强制交卷
     *
     * @throws IOException
     */
    @Around("execution(* com.cloud.exam.controller.ActivityFlowController.forceFinishExam(..) )")
    public Object forceFinishExam(ProceedingJoinPoint pjp) throws Throwable {
        //执行主过程
        Object proceed = pjp.proceed();
        //参数值
        Object[] args = pjp.getArgs();
        String  identityCard = (String) args[0];
        QueryWrapper<DrawResult> qw = new QueryWrapper();
        qw.eq("identity_card",identityCard);
        DrawResult one = drawResultService.getOne(qw);
        if (ObjectUtils.isNotNull(one) && ObjectUtils.isNotNull(one.getUserId())) {
            Long userId = one.getUserId();
            WebSocketServer.sendInfo(ExamConstants.MESSAGE_KAOSHI+ExamConstants.EXAM_FORCE_FINISH+"&"+userId,userId+"");
        }
        return proceed;
    }

    public Set<Long>  getMinitorUserIds(String identityId){
        QueryWrapper<DrawResult> qw = new QueryWrapper();
        qw.eq("identity_card",identityId);
        DrawResult one = drawResultService.getOne(qw);
        List<ExamManageGroupRel> ll = examService.getMinitorUserByExamId(one.getAcId());
        Set<Long>  userIds = new HashSet<>();
        ll.stream().forEach(e->{
            if(e.getManagegroupId()==1){
                userIds.add(e.getMemberId());
            }
        });

        return userIds;
    }


    public void createMessage(Exam ex,Object object,String flag,Object o) throws IOException {
        if(object instanceof Exam){
            Exam exam = (Exam)object;
            QueryWrapper qw = new QueryWrapper();
            qw.eq("ac_id", ((Exam) object).getId());
            List<DrawResult> list = drawResultService.list(qw);
            for (DrawResult dr:list) {
                DrawResultVO vo = new DrawResultVO();
                BeanUtils.copyProperties(dr,vo);
                ExamPlace byId = examPlaceService.getById(dr.getPlaceId());
                vo.setPlaceName(byId==null?"":byId.getPlaceName());
                if(exam.getType()==0){
                    Integer totalTime = paperService.getById(dr.getPaperId()).getTotalTime();
                    vo.setExamTime(String.valueOf(totalTime));
                }else if(exam.getType()==1){
                    Date startTime = exam.getStartTime();
                    Date endTime = exam.getEndTime();
                    long l = (endTime.getTime() - startTime.getTime()) / 1000 / 60;
                    vo.setExamTime(String.valueOf(l));
                }
                sendMessage(exam,vo,flag,o);
            }
        }else if(object instanceof DrawResult){
            DrawResult dr = (DrawResult)object;
            DrawResultVO v = new DrawResultVO();
            BeanUtils.copyProperties(dr,v);
            //Exam exam = examService.getById(dr.getAcId());
            sendMessage(ex,v,flag,o);
        }
    }
    public void sendMessage(Exam exam, DrawResultVO dr, String flag,Object o) throws IOException {
        try {
            UserActivityMessage uam = new UserActivityMessage();
            uam.setIsRead(ExamConstants.IS_NOT_READ);
            uam.setUserId(dr.getUserId());
            uam.setMessage(UserMessageUtils.createMessage(exam,dr,flag,o));
            uam.setCreateTime(new Date());
            userActivityMessageService.save(uam);
            WebSocketServer.sendInfo(UserMessageUtils.createMessage(exam,dr,flag,o),dr.getUserId()+"");
        }catch (Exception e){
            e.printStackTrace();
            logger.error("sendMessage保存消息失败活动id:{}，准考证号：{}",exam.getId(),dr.getIdentityCard());
        }
    }

    private Method getMethod(JoinPoint joinPoint){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method;
    }

    private static final Logger logger = LoggerFactory.getLogger(UserActAspect.class);
}
