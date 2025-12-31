package com.cloud.exam.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.core.ExamConstants;
import com.cloud.exam.annotation.RepeateRequestAnnotation;
import com.cloud.exam.annotation.UserActionAnnotation;
import com.cloud.exam.dao.ErrorQuestionDao;
import com.cloud.exam.model.exam.*;
import com.cloud.exam.service.*;
import com.cloud.exam.utils.CommonPar;
import com.cloud.exam.utils.Tools;
import com.cloud.exam.utils.word.WordImportUtil;
import com.cloud.exam.vo.DrawResultVO;
import com.cloud.exam.vo.ExamVO;
import com.cloud.exam.vo.StudentAnswerVO;
import com.cloud.exam.weboffice.feign.WebOfficeClient;
import com.cloud.exception.ResultMesCode;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.user.AppUser;
import com.cloud.model.user.LoginAppUser;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.utils.DateConvertUtils;
import com.cloud.utils.RedisUtils;
import com.cloud.utils.Validator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author md
 * @date 2021/3/26 16:50
 * 在线考试流程
 */
@Slf4j
@RestController
@RefreshScope
@Api(value = "在线考试流程处理类")
public class ActivityFlowController {

    @Resource
    private ExamService examService;
    @Resource
    private QuestionService questionService;
    @Resource
    private StudentAnswerService studentAnswerService;
    @Resource
    private SysDepartmentFeign sysDepartmentFeign;
    @Resource
    private DrawResultService drawResultService;
    @Resource
    private UserActivityMessageService userActivityMessageService;
    @Resource
    private PaperService paperService;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private WebOfficeClient webOfficeClient;
    @Value("${weboffice.dir}")
    private String webofficeDir;
    @Value("${weboffice.pdfDir}")
    private String webofficePdfDir;

    // 影像缩略图访问地址
    @Value("${localUrlPrefix}")
    private String localUrlPrefix;
    // 文件服务器地址
    @Value(value = "${file_server}")
    private String fileServer;
    private static final Logger logger = LoggerFactory.getLogger(ActivityFlowController.class);

    @Resource
    ErrorQuestionDao errorQuestionDao;
    @Resource
    CollectionQuestionService collectionQuestionService;
    @Resource
    private ExamKpPersonAvgScoreService examKpPersonAvgScoreService;


    /**
     * todo:
     * 1、登录（准考证号：随机生成）
     * 2、考试须知
     * 3、活动点击开始、活动过程保存
     * 4、活动提交
     * 5、活动自动判卷
     * 6、判卷消息发送
     * 7、服务端倒计时功能
     */


    /**
     * 考生登录后需要准考证号和身份证号，然后页面缓存考生试卷，考试开始后直接分发
     *
     * @param identityCard
     * @return
     */
    @ApiOperation(value = "考生登录考试")
    @ApiImplicitParam(name = "identityCard", value = "准考证号", dataType = "String")
    @RequestMapping(value = "/examLogin", method = RequestMethod.GET)
    @UserActionAnnotation(ActionType = 1)
    public ApiResult userExamLogin(String identityCard) {
        AppUser appUser = AppUserUtil.getLoginAppUser();
        QueryWrapper<DrawResult> qw = new QueryWrapper();
        qw.eq("identity_card", identityCard);
        DrawResult one = drawResultService.getOne(qw);

        if (ObjectUtil.isEmpty(identityCard) || ObjectUtil.isEmpty(one)) {
            throw new IllegalArgumentException("请输入有效的准考证号。");
        }
        Exam exam = examService.getById(one.getAcId());
        if (exam.getType() == 0) {
            if (one.getLoginDate() != null) {
                Paper paper = paperService.getById(one.getPaperId());
                //考生中途退出又登录考试，需要判断是否还有剩余考试时间
                Date loginDate = one.getLoginDate();
                long l = System.currentTimeMillis() - loginDate.getTime();//距离上次登录过了多长时间
                if (l >= paper.getTotalTime() * 60 * 1000) {
                    return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_PAPERTIME_OUT.getResultCode(), ResultMesCode.EXAM_PAPERTIME_OUT.getResultMsg(), null);
                }

            }
        }
        DrawResultVO v = new DrawResultVO();
        if (exam.getExamStatus().equals(ExamConstants.EXAM_WAIT_JUDGE) || exam.getExamStatus().equals(ExamConstants.EXAM_FINISH)) {
            return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_USER_STATUS_FINISHED.getResultCode(), ResultMesCode.EXAM_USER_STATUS_FINISHED.getResultMsg(), null);
        }
        if (Validator.isEmpty(one)) {
            return ApiResultHandler.buildApiResult(ResultMesCode.INDENTITY_CARD_WRONG.getResultCode(), ResultMesCode.INDENTITY_CARD_WRONG.getResultMsg(), null);
        }
        if (one.getUserStatus().equals(ExamConstants.EXAM_WAIT_JUDGE)) {
            return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_USER_STATUS_COMMITED.getResultCode(), ResultMesCode.EXAM_USER_STATUS_COMMITED.getResultMsg(), null);
        }
        /*if(!exam.getExamStatus().equals(ExamConstants.ACTIVITY_EXAM_START)){
            return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_NOT_STARTED.getResultCode(), ResultMesCode.EXAM_NOT_STARTED.getResultMsg(), null);
        }*/
        if (one.getUserStatus().equals(ExamConstants.EXAM_CONCELL)) {
            return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_USER_STATUS_CONCELL.getResultCode(), ResultMesCode.EXAM_USER_STATUS_CONCELL.getResultMsg(), null);
        }
        if (one.getUserStatus().equals(ExamConstants.EXAM_FINISH)) {
            return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_USER_STATUS_FINISHED.getResultCode(), ResultMesCode.EXAM_USER_STATUS_FINISHED.getResultMsg(), null);
        }
        if (exam.getType() == 0 && !one.getUserId().equals(appUser.getId())) {
            throw new IllegalArgumentException("您当前输入的准考证号和当前登录用户不匹配，请检查后重试。");
        }
        one.setUserStatus(ExamConstants.EXAM_YES_LOGIN);
        BeanUtils.copyProperties(one, v);
        v.setExamType(exam.getType());
        drawResultService.saveOrUpdate(one);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "登录成功。。。", v);
    }


    @ApiOperation(value = "获取活动详情")
    @ApiImplicitParam(name = "identityId", value = "准考证号", dataType = "String")
    @RequestMapping(value = "/getExamDetails", method = RequestMethod.GET)
    public ApiResult getExamDetails(String identityId) {
        QueryWrapper<DrawResult> qw = new QueryWrapper();
        qw.eq("identity_card", identityId);
        DrawResult one = drawResultService.getOne(qw);
        Exam byId = examService.getById(one.getAcId());
        ExamVO vo = new ExamVO();
        vo.setScreenRecord(byId.getScreenRecord());
        vo.setExamName(byId.getName());
        vo.setId(byId.getId());
        vo.setDescribe(byId.getDescribe());
        if (byId.getType() == 0) {
            vo.setPaperName(paperService.getById(one.getPaperId()).getPaperName());
            vo.setType(paperService.getById(one.getPaperId()).getType());
        } else {
            vo.setType(byId.getType());
        }
        vo.setExamDate(DateConvertUtils.getPattenTime(byId.getStartTime(), byId.getEndTime()));
        vo.setIsSign(byId.getIsSign());
        vo.setScorechartsId(byId.getScorechartsId());
        vo.setInitScore(byId.getInitialScore());
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "登录成功。。。", vo);
    }

    /**
     * 考生点击开始考试判断当前考试状态
     *
     * @param identityId
     * @return 返回考试的时间，考试的状态，超时时间
     */
    @ApiOperation(value = "考生点击开始考试")
    @ApiImplicitParam(name = "identityId", value = "准考证号", dataType = "String")
    @RequestMapping(value = "/startExamByStudent", method = RequestMethod.GET)
    public ApiResult startExamByStudent(String identityId) {

        QueryWrapper qw = new QueryWrapper();
        qw.eq("identity_card", identityId);
        DrawResult one = drawResultService.getOne(qw);
        Paper paper = paperService.getById(one.getPaperId());
        Exam byId = examService.getById(one.getAcId());
        Integer isFix = byId.getIsFix();//1严格0自由
        Date startTime = byId.getStartTime();
        Date endTime = byId.getEndTime();
        Date loginDate = one.getLoginDate();
        Long extTime = 0L;
        Integer status = byId.getExamStatus();
        JSONObject jsonObject = new JSONObject();
        //考试已经开始
        if (status.equals(ExamConstants.ACTIVITY_EXAM_START) || status.equals(ExamConstants.ACTIVITY_WAIT_JUDGE)) {
            if (!Validator.isEmpty(loginDate)) {
                //学员中途退出后 继续登录考试
                Long time = System.currentTimeMillis() - loginDate.getTime();//上次登录距离现在的时间

                if (isFix == 0) {
                    //自由考试
                    long l = paper.getTotalTime() * 60 * 1000 - time;//剩余试卷考试时间
                    long l1 = endTime.getTime() - System.currentTimeMillis();//距离活动结束时间的时长
                    if (l > l1) {
                        //计算剩余时间
                        extTime = l1 / 1000;
                    } else {
                        extTime = l / 1000;
                    }
                } else {
                    //固定考试
                    extTime = (paper.getTotalTime() * 60 * 1000 - time) / 1000;
                }

                jsonObject.put("flag", true);
                //剩余考试时间
                jsonObject.put("extTime", extTime);
                //离考试开始时间
                jsonObject.put("overTime", 0);
            } else {
                //学员首次进入考试
                if (isFix == 0) {
                    //自由考试
                    if (System.currentTimeMillis() + paper.getTotalTime() * 60 * 1000 > endTime.getTime()) {
                        //计算剩余时间
                        extTime = endTime.getTime() - System.currentTimeMillis();
                    } else {
                        extTime = paper.getTotalTime() * 60L;
                    }
                } else {
                    //固定考试,考试已经开始
                    extTime = (paper.getTotalTime() * 60L * 1000 - (System.currentTimeMillis() - startTime.getTime())) / 1000;
                }
                //extTime  =paper.getTotalTime()*60L;
                jsonObject.put("flag", true);
                jsonObject.put("extTime", extTime);
                jsonObject.put("overTime", 0);
            }
        } else {
            //考试还未开始
            //自由考试活动
            if (isFix == 0) {
                if (paper.getTotalTime() * 60 * 1000 > (endTime.getTime() - startTime.getTime())) {
                    //试卷时间超出活动结束时间
                    extTime = (endTime.getTime() - startTime.getTime()) / 1000;
                } else {
                    extTime = paper.getTotalTime() * 60L;
                }

            } else {
                //固定考试
                extTime = paper.getTotalTime() * 60L;
            }
            jsonObject.put("flag", false);
            jsonObject.put("extTime", extTime);
            Long overTime = byId.getStartTime().getTime() - System.currentTimeMillis();
            jsonObject.put("overTime", overTime / 1000);
        }

        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "开始进入考试。。。", jsonObject);
    }


    /**
     * @return 返回值 返回考试的结果，选择的答案是否正确
     */
    @ApiOperation(value = "学员点击开始考试获取试题/答题过程中预览结果", notes = "学员点击开始考试获取试题/答题过程中预览结果")
    @ApiImplicitParam(name = "identityId", value = "学员准考证号", required = true, dataType = "String")
    @RequestMapping(value = "/getStudentAnswer", method = RequestMethod.GET)
    @UserActionAnnotation(ActionType = 6)
    public ApiResult getStudentAnswer(String identityId) {
        LoginAppUser loginAppUser = sysDepartmentFeign.getLoginAppUser();
        QueryWrapper qw = new QueryWrapper();
        qw.eq("identity_card", identityId);
        DrawResult one = drawResultService.getOne(qw);
        Exam exam = examService.getById(one.getAcId());

        List objects = redisUtils.lGet("exam:" + one.getAcId() + ":" + identityId, 0, -1);
        if (!Validator.isEmpty(objects)) {
            List<StudentAnswerVO> list = (List<StudentAnswerVO>) objects.get(0);
            for (StudentAnswerVO vo : list) {
                // 增加是否收藏逻辑
                QueryWrapper<CollectionQuestion> collectionQuestionQueryWrapper = new QueryWrapper<>();
                collectionQuestionQueryWrapper.eq("user_id", one.getUserId());
                collectionQuestionQueryWrapper.eq("question_id", vo.getQuestionId());
                CollectionQuestion collectionQuestion = collectionQuestionService.getOne(collectionQuestionQueryWrapper);
                long collectionId = collectionQuestion == null ? 0l : collectionQuestion.getId();
                vo.setCollectionId(collectionId);
            }
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "预览试题成功。。。", objects.get(0));
        }

        if (Validator.isEmpty(one.getLoginDate())) {
            one.setLoginDate(new Date());
        }
        one.setUserStatus(ExamConstants.EXAM_START);
        String key = "exam:" + one.getAcId() + ":" + one.getPaperId();
//        List list = redisUtils.lGet(key, 0, -1);
        List list = null;
        if (CollectionUtils.isEmpty(list)) {
            Long paperId = one.getPaperId();
            List<Question> list2 = examService.getAllQuestionsBypaperId(paperId);
            List<StudentAnswerVO> ll = new ArrayList<>();
            for (Question q : list2) {
                StudentAnswerVO vo = new StudentAnswerVO();
                vo.setLocalUrlPrefix(localUrlPrefix);
                vo.setQuestionId(q.getId());
                vo.setQuestion(q.getQuestion());
                vo.setOptions(q.getOptions());
                vo.setType(q.getType());
                vo.setPaperId(paperId);
                vo.setModelUrl(q.getModelUrl());
                vo.setFileAddr(fileServer);
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
                Double questionScoreById = questionService.getQuestionScoreById(paperId, q.getId());
                vo.setQuestionScore(questionScoreById);

                // 增加是否收藏逻辑
                QueryWrapper<CollectionQuestion> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("user_id", one.getUserId());
                queryWrapper.eq("question_id", vo.getQuestionId());
                CollectionQuestion collectionQuestion = collectionQuestionService.getOne(queryWrapper);
                long collectionId = collectionQuestion == null ? 0L : collectionQuestion.getId();
                vo.setCollectionId(collectionId);
                vo.setTypeName(CommonPar.question_type.get(vo.getType().toString()).toString());
                ll.add(vo);
            }
            redisUtils.lSet("exam:" + exam.getId() + ":" + paperId, ll);
            drawResultService.saveOrUpdate(one);
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取考试试题成功。。。", ll);

        }
        drawResultService.saveOrUpdate(one);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取考试试题成功。。。", list.get(0));
    }

    /**
     * 定时将学员考试结果存入redis
     */
    @ApiOperation(value = "定时保存学员考试过程", notes = "保存学员考试过程缓存信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "identityId", value = "准考证号", dataType = "String"),
            @ApiImplicitParam(name = "list", value = "所选试题答案集合", dataType = "studentAnswerVO")})
    @RequestMapping(value = "/saveStudentAnswer", method = RequestMethod.POST)
    public ApiResult saveStudentAnswer(@RequestParam String identityId, @RequestBody List<StudentAnswerVO> list) {

        QueryWrapper<DrawResult> dr = new QueryWrapper<>();
        dr.eq("identity_card", identityId);
        DrawResult one = drawResultService.getOne(dr);
        List<Object> objects = redisUtils.lGet("exam:" + one.getAcId() + ":" + identityId, 0, -1);
        for (StudentAnswerVO vo : list) {
            Question byId = questionService.getById(vo.getQuestionId());
            //只有制图整饰和在线标绘在存入redis时同时持久化到数据库，再提交试卷时只需提交情报分析考试答案，无需再存这两个
            if (null != vo.getIntType() && vo.getIntType() != 1) {
                StudentAnswer sa = new StudentAnswer();
                sa.setQuestionId(vo.getQuestionId());
                sa.setStudentId(one.getUserId());
                sa.setPaperId(one.getPaperId());
                sa.setPaperType(vo.getType());
                JSONObject jso = new JSONObject();
                jso.put("text", vo.getStuAnswers());
                sa.setStuAnswer(jso.toJSONString());
                sa.setType(vo.getType());
                sa.setIntType(vo.getIntType());
                sa.setAcId(one.getAcId());
                sa.setPdType(byId.getPdType());
                studentAnswerService.save(sa);
            }

            //JSONObject jso = JSONObject.parseObject(vo.getStuAnswers().toString());;
            //jso.put("text",vo.getStuAnswers());
            vo.setStuAnswer(vo.getStuAnswers().toString());
            vo.setPaperId(one.getPaperId());
            vo.setStudentId(one.getUserId());
            vo.setPaperType(byId.getType());
            vo.setQuestion(byId.getQuestion());
            vo.setOptions(byId.getOptions());
            vo.setLocalUrlPrefix(localUrlPrefix);
            vo.setFileAddr(fileServer);
            if (objects != null && objects.size() > 0) {
                List<StudentAnswerVO> o = (List<StudentAnswerVO>) objects.get(0);
                o.stream().forEach(e -> {
                    if (e.getQuestionId().equals(vo.getQuestionId())) {
                        vo.setMark(e.getMark());
                    }
                });
            }
        }
        redisUtils.delKeys("exam:" + one.getAcId() + ":" + identityId);
        redisUtils.lSet("exam:" + one.getAcId() + ":" + identityId, list, ExamConstants.EXAM_STUDENT_ANSWER_DETAILS_TIME);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "提交成功。", null);
    }


    /**
     * 理论考试
     * 实操考试
     * 情报分析考试：（
     * 1、地图数据的保存
     * 2、制图整饰数据的保存
     * 3、情报分析文档的保存（step1：点击作答，为学员分配考试模板，展示在线编辑模板；step2:作答过程的保存，点击保存后保存到该学员答案在weboffice下，返回给前端一个url；
     * step3:点击提交,保存该路径到学生答案表中作为学生的答案）
     * ）
     *
     * @param identityId
     * @param list
     * @return
     */
    @ApiOperation(value = "用户点击交卷", notes = "用户点击交卷")
    @ApiImplicitParams({@ApiImplicitParam(name = "identityId", value = "准考证号", dataType = "String"),
            @ApiImplicitParam(name = "list", value = "试题答案集合", dataType = "studentAnswer")})
    @RequestMapping(value = "/finishStudentAnswer", method = RequestMethod.POST)
    @UserActionAnnotation(ActionType = 3)
    @RepeateRequestAnnotation
    public ApiResult finishStudentAnswer(@RequestParam String identityId, @RequestBody List<StudentAnswerVO> list) {
        boolean b1 = list.stream().anyMatch(e -> e.getType() > 3);
        QueryWrapper<DrawResult> dr = new QueryWrapper<>();
        dr.eq("identity_card", identityId);
        DrawResult one = drawResultService.getOne(dr);
        if (one.getUserStatus().equals(ExamConstants.ACTIVITY_WAIT_JUDGE) || one.getUserStatus().equals(ExamConstants.ACTIVITY_FINISH)) {
            throw new IllegalArgumentException("您已提交答案。");
        }
        Exam exam = examService.getById(one.getAcId());
        QueryWrapper<DrawResult> qw = new QueryWrapper<>();
        qw.eq("ac_id", one.getAcId());
        List<DrawResult> list1 = drawResultService.list(qw);
        boolean b = list1.stream().allMatch(e -> e.getUserStatus().equals(ExamConstants.EXAM_WAIT_JUDGE));
        if (b) {
            exam.setExamStatus(ExamConstants.ACTIVITY_WAIT_JUDGE);
            examService.saveOrUpdate(exam);
        }
        Long paperId = one.getPaperId();

        if (one.getPaperType().equals(ExamConstants.PAPER_QINGXI)) {
            //提交情析试卷
            one.setUserStatus(ExamConstants.EXAM_WAIT_JUDGE);
            String costTime = DateConvertUtils.longTimeToDay(one.getLoginDate(), new Date());
            one.setCostTime(costTime);
            drawResultService.saveOrUpdate(one);
            for (StudentAnswerVO vo : list) {
                logger.info(">{}  情报分析试卷存储");
                StudentAnswer sa = new StudentAnswer();
                sa.setQuestionId(vo.getQuestionId());
                Question question = questionService.getById(vo.getQuestionId());
                //String  modelUrl= question.getModelUrl();
                //String ext = modelUrl.substring(modelUrl.lastIndexOf("."));
                sa.setStudentId(one.getUserId());
                sa.setPaperId(paperId);
                sa.setPaperType(vo.getType());
                sa.setCreateTime(new Date());
                //JSONObject jso = new JSONObject();
                sa.setType(vo.getType());
                sa.setStuAnswer(vo.getStuAnswers().toString());
                sa.setType(vo.getType());
                sa.setAcId(one.getAcId());
                sa.setPdType(question.getPdType());
                //vo.setStuAnswer(vo.getStuAnswers().toString());
                /*try {
                    boolean b2 = FileUtils.ifExists(webofficeDir + File.separator+ (identityId + vo.getQuestionId() + ext));
                    if(b2){
                        //doc转成pdf
                        WordPdfUtil.doc2pdf(webofficeDir+ File.separator+(identityId+vo.getQuestionId()+ext),webofficeDir+  File.separator+ (identityId + vo.getQuestionId() + ".pdf"));
                    }
                    String test = webOfficeClient.test(identityId + vo.getQuestionId());
                    if(test.contains("pdf")){
                        FileUtils.moveFile(test,webofficeDir);
                    }
                    File file = new File(webofficeDir + File.separator+ (identityId + vo.getQuestionId()+".pdf"));
                    if(file.exists()){
                        //将生成的pdf保存到 fastdfs
                        String noPdfUrl = FileFastdfsUtils.uploadFile(fileDoMu(new File(webofficeDir + File.separator+ (identityId + vo.getQuestionId()+".pdf"))));
                        jso.put("text", noPdfUrl);
                    }else{
                        jso.put("text", identityId+ext);
                    }

                }catch (Exception e){
                    logger.error(identityId + vo.getQuestionId()+"提交清析考试生成pdf失败。。。");
                }*/
                studentAnswerService.save(sa);
                //vo.setId(sa.getId());
                //vo.setPaperId(paperId);
                //vo.setStudentId(one.getUserId());
            }
            //清晰考试直接读取服务器上的文件，不保存缓存
            //return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "交卷成功。", null);
        } else {

            String costTime = DateConvertUtils.longTimeToDay(one.getLoginDate(), new Date());
            one.setCostTime(costTime);
            double totalScore = 0L;
            Map<Object, Object> map = new LinkedHashMap<>();
            List<StudentAnswerDetails> list2 = new ArrayList();
            list2.add(new StudentAnswerDetails());
            list2.add(new StudentAnswerDetails());
            list2.add(new StudentAnswerDetails());
            list2.add(new StudentAnswerDetails());
            map.put("list", list2);
            map.put("name", exam.getName());
            for (StudentAnswerVO vo : list) {
                StudentAnswer sa = new StudentAnswer();
                sa.setQuestionId(vo.getQuestionId());
                sa.setStudentId(one.getUserId());
                sa.setPaperId(paperId);
                sa.setPaperType(ExamConstants.PAPER_LILUN);
                sa.setType(vo.getType());
                vo.setStuAnswer(vo.getStuAnswers() == null ? vo.getStuAnswer() : vo.getStuAnswers().toString());
                vo.setStuAnswer(vo.getStuAnswers() == null ? null : vo.getStuAnswers().toString());
                sa.setCreateTime(new Date());
                try {
                    double v = judgeStudentAnswer(vo, map);
                    totalScore += v;
                    sa.setActualScore(v);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("考生提交答案后判断客观题失败。。。");
                }
                sa.setStuAnswer(vo.getStuAnswers() == null ? vo.getStuAnswer() : vo.getStuAnswers().toString());
                sa.setAcId(one.getAcId());
                Question question = questionService.getById(vo.getQuestionId());
                sa.setPdType(question.getPdType());
                studentAnswerService.save(sa);
                vo.setId(sa.getId());
                vo.setPaperId(paperId);
                vo.setStudentId(one.getUserId());
                vo.setCostTime(costTime);
                vo.setCreateTime(new Date());
                vo.setLocalUrlPrefix(localUrlPrefix);
                vo.setFileAddr(fileServer);
            }
            if (!b1) {
                one.setUserStatus(ExamConstants.EXAM_FINISH);
                map.put("flag", true);
            } else {
                one.setUserStatus(ExamConstants.EXAM_WAIT_JUDGE);
                map.put("flag", false);
            }
            one.setScore(totalScore);
            map.put("score", totalScore);
            drawResultService.saveOrUpdate(one);

            if (one.getUserStatus() == ExamConstants.EXAM_FINISH) {
                // 错误试题收录
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("studentId", one.getUserId());
                errorMap.put("paperId", one.getPaperId());
                List<Map<String, Object>> errorList = errorQuestionDao.errorList(errorMap);
                if (errorList != null && errorList.size() > 0) {
                    Map<String, Object> parMap = new HashMap<>();
                    for (Map<String, Object> m : errorList) {
                        // 根据用户id和试题id查询是否已记录
                        parMap.put("userId", one.getUserId());
                        parMap.put("questionId", Long.valueOf(m.get("question_id").toString()));
                        List<Map<String, Object>> byPar = errorQuestionDao.findByPar(parMap);
                        if (byPar == null || byPar.size() == 0) {
                            parMap.put("question", m.get("question"));
                            parMap.put("type", CommonPar.question_type.get(m.get("type").toString()));
                            parMap.put("createTime", new Timestamp(System.currentTimeMillis()));
                            errorQuestionDao.saveInfo(parMap);
                        } else {
                            parMap.put("id", Long.valueOf(byPar.get(0).get("id").toString()));
                            parMap.put("createTime", new Timestamp(System.currentTimeMillis()));
                            errorQuestionDao.updateInfo(parMap);
                        }
                    }
                }
            }
            redisUtils.delKeys("exam:" + one.getAcId() + ":" + identityId);
            redisUtils.lSet("exam:" + one.getAcId() + ":" + identityId, list);
            Tools.putJudgeCache(identityId, map);
        }
        //查询此活动下所有用户是否交完卷
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("ac_id", exam.getId());
        List<DrawResult> list3 = drawResultService.list(queryWrapper);

        if (list3.stream().allMatch(drawResult -> drawResult.getUserStatus() == 4)) {
            exam.setExamStatus(ExamConstants.EXAM_FINISH);
            examService.saveOrUpdate(exam);
            examKpPersonAvgScoreService.calculate(exam.getId());
        }
        boolean flag = true;
        if (list3.stream().anyMatch(d -> d.getUserStatus() == 0 || d.getUserStatus() == 1 || d.getUserStatus() == 6)) {
            flag = false;
        }
        if (flag) {
            exam.setExamStatus(ExamConstants.EXAM_WAIT_JUDGE);
            examService.saveOrUpdate(exam);
        }

        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "交卷成功。", null);
    }

    @Autowired
    EvalNewService evalNewService;

    public static MultipartFile fileDoMu(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        return new MockMultipartFile(file.getName(), file.getName(), Files.probeContentType(file.toPath()), inputStream);
    }

    //客观题自动判卷
    @ApiOperation(value = "客观题自动判卷", notes = "客观题自动判卷")
    @ApiImplicitParam(name = "identityId", value = "学员准考证号", required = true, dataType = "String")
    @RequestMapping(value = "/judgeObjectiveQustion", method = RequestMethod.GET)
    public ApiResult judgeObjectiveQustion(String identityId) {
        try {
            QueryWrapper wq = new QueryWrapper();
            wq.eq("identity_card", identityId);
            DrawResult dr = drawResultService.getOne(wq);
            Object judgeCache = Tools.getJudgeCache(identityId);
            if (!Validator.isEmpty(judgeCache)) {
                Map<Object, Object> map = (Map<Object, Object>) judgeCache;
                List<StudentAnswerDetails> ll = new ArrayList<>();
                for (Map.Entry<Object, Object> entries : map.entrySet()) {
                    if ("list".equals(entries.getKey())) {
                        List<StudentAnswerDetails> value = (List<StudentAnswerDetails>) entries.getValue();
                        value.stream().forEach(e -> {
                            if (e.getQuestionType() != null) {
                                ll.add(e);
                            }
                        });
                    }
                }
                map.remove("list");
                map.put("list", ll);
                // 计算知识点能力
                new Thread(new Runnable() {
                    @SneakyThrows
                    @Override
                    public void run() {
                        evalNewService.makeEvaluation(dr.getAcId());
                    }
                }).start();
                return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "交卷成功。", map);
            }
            // 计算知识点能力
            new Thread(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    evalNewService.makeEvaluation(dr.getAcId());
                }
            }).start();
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "交卷成功。", judgeCache);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    //考生添加试题标识
    @ApiOperation(value = "考生添加试题标识")
    @ApiImplicitParams({@ApiImplicitParam(name = "identityId", value = "准考证号", dataType = "String"),
            @ApiImplicitParam(name = "questionId", value = "试题ID", dataType = "Long"),
            @ApiImplicitParam(name = "flag", value = "是否标记", dataType = "Boolean")
    })
    @RequestMapping(value = "/addMark2Qustion", method = RequestMethod.GET)
    public ApiResult addMark2Qustion(String identityId, Long questionId, boolean flag) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq("identity_card", identityId);
        DrawResult one = drawResultService.getOne(qw);
        List objects = redisUtils.lGet("exam:" + one.getAcId() + ":" + identityId, 0, -1);
        List<StudentAnswerVO> ll = new ArrayList<>();
        if (objects == null || objects.size() < 1) {
            String key = "exam:" + one.getAcId() + ":" + one.getPaperId();
            List list = redisUtils.lGet(key, 0, -1);
            ll = (List<StudentAnswerVO>) list.get(0);
        } else {
            ll = (List<StudentAnswerVO>) objects.get(0);
        }

        StudentAnswerVO studentAnswerVO = ll.stream().filter(e -> e.getQuestionId().equals(questionId)).findFirst().get();
        studentAnswerVO.setMark(flag);
        redisUtils.delKeys("exam:" + one.getAcId() + ":" + identityId);
        redisUtils.lSet("exam:" + one.getAcId() + ":" + identityId, ll, ExamConstants.EXAM_STUDENT_ANSWER_DETAILS_TIME);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "添加标识成功。", null);
    }

    //考生提交理论试卷后自动判断客观题
    public double judgeStudentAnswer(StudentAnswerVO sa, Map<Object, Object> map) {
        double score = 0L;
        Question byId = questionService.getById(sa.getQuestionId());
        //BeanUtils.copyProperties(byId,sav);
        sa.setQuestion(byId.getQuestion());
        sa.setOptions(byId.getOptions());
        //sa.setCostTime(costTime);
        JSONObject js = new JSONObject();
        js.put("text", "");
        js.put("url", "");
        sa.setKeywords(byId.getKeywords() == null ? js.toJSONString() : byId.getKeywords());
        sa.setAnalysis(byId.getAnalysis() == null ? js.toJSONString() : byId.getAnalysis());
        sa.setAnswer(byId.getAnswer() == null ? js.toJSONString() : byId.getAnswer());
        if (sa.getType() == 1 || sa.getType() == 3) {
            JSONObject jsonObject = JSONObject.parseObject(sa.getStuAnswer());
            JSONObject jsonObject1 = JSONObject.parseObject(byId.getAnswer());
            List<StudentAnswerDetails> list = (List<StudentAnswerDetails>) map.get("list");
            StudentAnswerDetails studentAnswerDetails = new StudentAnswerDetails();
            if (sa.getType() == 1) {
                studentAnswerDetails = list.get(0);
                studentAnswerDetails.setQuestionType(1);
            } else {
                studentAnswerDetails = list.get(2);
                studentAnswerDetails.setQuestionType(3);
            }
            //StudentAnswerDetails studentAnswerDetails = (StudentAnswerDetails)map.get(sa.getType());
            Integer totalNumber = studentAnswerDetails.getTotalNumber();
            Integer correctNumber = studentAnswerDetails.getCorrectNumber();
            Integer wrongNumber = studentAnswerDetails.getWrongNumber();
            double totalScore = studentAnswerDetails.getTotalScore();

            totalNumber++;
            if (jsonObject != null && jsonObject1 != null
                    && jsonObject.getString("text").equals(WordImportUtil.fullWidth2half(jsonObject1.getString("text")))) {
                sa.setFlag(true);
                score = sa.getQuestionScore();
                totalScore += score;
                correctNumber++;
                studentAnswerDetails.setCorrectNumber(correctNumber);
                studentAnswerDetails.setTotalScore(totalScore);
            } else {
                wrongNumber++;
                studentAnswerDetails.setWrongNumber(wrongNumber);
            }
            studentAnswerDetails.setQuestionScore(sa.getQuestionScore());
            studentAnswerDetails.setTotalNumber(totalNumber);
        } else if (sa.getType() == 2) {
            List<StudentAnswerDetails> list = (List<StudentAnswerDetails>) map.get("list");
            StudentAnswerDetails studentAnswerDetails = list.get(1);
            studentAnswerDetails.setQuestionType(2);
            //StudentAnswerDetails studentAnswerDetails = (StudentAnswerDetails)map.get(sa.getType());
            Integer totalNumber = studentAnswerDetails.getTotalNumber();
            totalNumber++;
            Integer correctNumber = studentAnswerDetails.getCorrectNumber();
            Integer wrongNumber = studentAnswerDetails.getWrongNumber();
            double totalScore = studentAnswerDetails.getTotalScore();
            JSONObject jsonObject1 = JSONObject.parseObject(sa.getStuAnswer());
            JSONArray o = JSONArray.parseArray(jsonObject1.getString("text"));
            if (!Validator.isEmpty(o) && o.size() > 0) {
                String value = "";
                for (int i = 0; i < o.size(); i++) {
                    if (i > 0) {
                        value += ",";
                    }
                    value += o.get(i);
                }
                JSONObject jos = JSONObject.parseObject(byId.getAnswer());
                String text = WordImportUtil.fullWidth2half(jos.getString("text"));
                String[] split = text.split(",");
                List ll = new ArrayList();
                for (int i = 0; i < split.length; i++) {
                    ll.add(split[i]);
                }
                Collections.sort(ll);
                String v = "";
                for (int i = 0; i < ll.size(); i++) {
                    if (i > 0) {
                        v += ",";
                    }
                    v += ll.get(i);
                }
                if (value.equals(v)) {
                    sa.setFlag(true);
                    score = sa.getQuestionScore();
                    totalScore += score;
                    correctNumber++;
                    studentAnswerDetails.setCorrectNumber(correctNumber);
                    studentAnswerDetails.setTotalScore(totalScore);
                } else {
                    wrongNumber++;
                    studentAnswerDetails.setWrongNumber(wrongNumber);
                }
                studentAnswerDetails.setQuestionScore(sa.getQuestionScore());
                studentAnswerDetails.setTotalNumber(totalNumber);
            } else {
                wrongNumber++;
            }
            studentAnswerDetails.setTotalNumber(totalNumber);
            studentAnswerDetails.setWrongNumber(wrongNumber);
            studentAnswerDetails.setTotalScore(totalScore);
        } else if (sa.getType() == 4) {
            String answer = byId.getAnswer();
            List s = new LinkedList();
            LinkedHashMap<String, String> jsonmap = JSON.parseObject(answer, new TypeReference<LinkedHashMap<String, String>>() {
            });
            for (Map.Entry<String, String> entry : jsonmap.entrySet()) {
                s.add(entry.getValue());
            }
            JSONObject jso = new JSONObject(true);
            jso.put("text", s.toArray());
            sa.setAnswer(jso.toJSONString());
            String stuAnswer = sa.getStuAnswer();
            JSONObject jsonObject = JSONObject.parseObject(stuAnswer);
            JSONArray text = (JSONArray) jsonObject.get("text");
            sa.setFlag(true);
            score = sa.getQuestionScore();
            if (s.size() != text.size()) {
                sa.setFlag(false);
                score = 0L;
            } else {
                for (int i = 0; i < s.size(); i++) {
                    if (!s.get(i).equals(text.get(i))) {
                        sa.setFlag(false);
                        score = 0L;
                    }
                }
            }
        } else if (sa.getType() == 9) {
            String rightAnswer = byId.getAnswer();
            String stuAnswers = (String) sa.getStuAnswers();
            List<StudentAnswerDetails> list = (List<StudentAnswerDetails>) map.get("list");

            StudentAnswerDetails studentAnswerDetails = list.get(3);
            studentAnswerDetails.setQuestionType(9);

            Integer totalNumber = studentAnswerDetails.getTotalNumber();
            Integer correctNumber = studentAnswerDetails.getCorrectNumber();
            Integer wrongNumber = studentAnswerDetails.getWrongNumber();
            double totalScore = studentAnswerDetails.getTotalScore();

            totalNumber++;
            if (rightAnswer != null && stuAnswers != null) {
                boolean b = checkLX(rightAnswer, stuAnswers);
                if (b == true) {
                    sa.setFlag(b);
                    score = sa.getQuestionScore();
                    totalScore += score;
                    correctNumber++;
                    studentAnswerDetails.setCorrectNumber(correctNumber);
                    studentAnswerDetails.setTotalScore(totalScore);
                } else {
                    wrongNumber++;
                    studentAnswerDetails.setWrongNumber(wrongNumber);
                }
            } else {
                wrongNumber++;
                studentAnswerDetails.setWrongNumber(wrongNumber);
            }
            studentAnswerDetails.setQuestionScore(sa.getQuestionScore());
            studentAnswerDetails.setTotalNumber(totalNumber);
        }
        return score;
    }

    public boolean checkLX(String rightAnswer, String stuAnswers) {
        int num = 0;
        Map stuMap = new HashMap();
        List<String> rightList = new ArrayList<>();
        String nstuAnswers = stuAnswers.substring(1, stuAnswers.length() - 2);
        String[] stuSplits = nstuAnswers.split("],");
        String nrightAnswers = rightAnswer.substring(1, rightAnswer.length() - 2);
        String[] rightSplits = nrightAnswers.split("],");
        if (stuSplits.length != rightSplits.length) {
            return false;
        } else {
            for (int i = 0; i < stuSplits.length; i++) {
                String value = stuSplits[i].substring(1);
                System.out.println("-----stuAnswer" + value);
                stuMap.put(value, 1);
            }
            for (int k = 0; k < rightSplits.length; k++) {
                String v = rightSplits[k].substring(1);
                System.out.println("-----rightAnswer" + v);
                if (stuMap.get(v) == null) {
                    return false;
                } else {
                    num += 1;
                }
            }
        }
        if (num == rightSplits.length) {
            return true;
        }
        return false;
    }

    /**
     * 获取用户消息
     *
     * @param page
     * @param size
     */
    @ApiOperation(value = "获取用户消息", notes = "获取用户消息")
    @ApiImplicitParams({@ApiImplicitParam(name = "page", value = "页数", dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = "数量", dataType = "Integer")
    })
    @RequestMapping(value = "/getAllNewsByUserId", method = RequestMethod.GET)
    public ApiResult distributionPapers(@RequestParam Integer page, @RequestParam Integer size) {
        Page<UserActivityMessage> pg = new Page(page, size);
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        QueryWrapper qw = new QueryWrapper();
        qw.eq("user_id", loginAppUser.getId());
        qw.orderByDesc("create_time");
        IPage page1 = userActivityMessageService.page(pg, qw);
        List<UserActivityMessage> list = page1.getRecords();
        for (UserActivityMessage uas : list) {
            uas.setIsRead("1");
            uas.setReadTime(new Date());
            if (System.currentTimeMillis() - uas.getCreateTime().getTime() < ExamConstants.EXT_TIME) {
                uas.setNew(true);
            }
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取用户信息成功。。。", page1);
    }

    /**
     * 获取用户考试记录
     */
    @ApiOperation(value = "获取用户考试记录", notes = "获取用户考试记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "page", value = "页数", dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = "数量", dataType = "Integer"),
            @ApiImplicitParam(name = "name", value = "名称", dataType = "String")
    })
    @RequestMapping(value = "/getUserExamHistory", method = RequestMethod.GET)
    public ApiResult getUserExamHistory(Integer page, Integer size, String name) {
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        Page pg = new Page(page, size);
        QueryWrapper<DrawResult> qw = new QueryWrapper();
        List<Integer> ll = new ArrayList();
        //ll.add(ExamConstants.ACTIVITY_WAIT_JUDGE);
        ll.add(ExamConstants.ACTIVITY_FINISH);
        qw.eq("user_id", loginAppUser.getId());
        qw.in("user_status", ll);
        qw.gt("paper_id", 0);
        qw.eq("exam_type", ExamConstants.EXAM_TYPE_LILUN);
        if (Validator.isNotNull(name)) {
            Set<Long> examIds = new HashSet<>();
            QueryWrapper wq = new QueryWrapper();
            wq.like("name", name);
            wq.eq("type", ExamConstants.EXAM_TYPE_LILUN);
            List<Exam> list = examService.list(wq);
            list.stream().forEach(e -> examIds.add(e.getId()));
            List<Long> paperIds = new ArrayList();
            QueryWrapper paw = new QueryWrapper();
            paw.like("paper_name", name);
            List<Paper> listPaper = paperService.list(paw);
            listPaper.stream().forEach(e -> paperIds.add(e.getId()));
            if (examIds.size() > 0 && paperIds.size() > 0) {
                qw.and(q -> q.in("ac_id", examIds).or().in("paper_id", paperIds));
            } else if (examIds.size() > 0) {
                qw.in("ac_id", examIds);
            } else if (paperIds.size() > 0) {
                qw.in("paper_id", paperIds);
            } else {
                qw.eq("id", -1);
            }

        }
        qw.orderByDesc("login_date");
        IPage page1 = drawResultService.page(pg, qw);
        page1.convert(dr -> {
            DrawResult d = (DrawResult) dr;
            DrawResultVO vo = new DrawResultVO();
            vo.setCostTime(d.getCostTime());
            vo.setScore(d.getScore());
            vo.setAbilityLevel(d.getAbilityLevel());
            vo.setExamName(examService.getById(d.getAcId()).getName());
            vo.setExamDate(DateConvertUtils.date2Str(examService.getById(d.getAcId()).getStartTime()));
            vo.setPaperName(paperService.getById(d.getPaperId()).getPaperName());
            vo.setUserStatus(d.getUserStatus());
            vo.setAcId(d.getAcId());
            vo.setIdentityCard(d.getIdentityCard());
            vo.setPaperType(paperService.getById(d.getPaperId()).getType());
            return vo;
        });
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取用户考试信息成功。。。", page1);
    }


    /**
     * 学员登录查看自己的考试试卷阅卷详情
     *
     * @param identityId
     * @return
     */
    @ApiOperation(value = "获取考生活动的阅卷详情")
    @ApiImplicitParam(name = "identityId", value = "准考证号", dataType = "String")
    @RequestMapping("/getJudgeDetails")
    public ApiResult getJudgeDetails(String identityId) {
        QueryWrapper<DrawResult> qw = new QueryWrapper<>();
        qw.eq("identity_card", identityId);
        DrawResult one = drawResultService.getOne(qw);
        Long userId = one.getUserId();
        Long paperId = one.getPaperId();
        QueryWrapper<StudentAnswer> qw1 = new QueryWrapper<>();
        qw1.eq("student_id", userId);
        qw1.eq("paper_id", paperId);
        List<StudentAnswerVO> ll = new ArrayList<>();
        List<StudentAnswer> list = studentAnswerService.list(qw1);
        for (StudentAnswer stu : list) {
            StudentAnswerVO sa = new StudentAnswerVO();
            BeanUtils.copyProperties(stu, sa);
            sa.setLocalUrlPrefix(localUrlPrefix);
            Question byId = questionService.getById(stu.getQuestionId());
            //BeanUtils.copyProperties(byId,sav);
            sa.setQuestion(byId.getQuestion());
            sa.setOptions(byId.getOptions());
            Double questionScoreById = questionService.getQuestionScoreById(paperId, byId.getId());
            sa.setQuestionScore(questionScoreById);
            JSONObject js = new JSONObject();
            js.put("text", "");
            js.put("url", "");
            sa.setKeywords(byId.getKeywords() == null ? js.toJSONString() : byId.getKeywords());
            sa.setAnalysis(byId.getAnalysis() == null ? js.toJSONString() : byId.getAnalysis());
            sa.setAnswer(byId.getAnswer() == null ? js.toJSONString() : byId.getAnswer());
            sa.setActualScore(stu.getActualScore());
            if (stu.getType() == 7) {
                String kpScores = stu.getKpScores();
                List<HashMap<String, Object>> list1 = new ArrayList<>();
                if (ObjectUtil.isNotEmpty(kpScores)) {
                    JSONArray jsonArray = JSONArray.parseArray(kpScores);
                    for (Object a : jsonArray) {
                        JSONObject x = (JSONObject) a;
                        HashMap<String, Object> userMap = new HashMap<>();
                        for (Map.Entry<String, Object> entry : x.entrySet()) {
                            userMap.put(entry.getKey(), entry.getValue());
                        }
                        list1.add(userMap);
                    }
                    sa.setKpDetails(list1);
                }
            }
            if (stu.getType() == 4) {
                String answer = byId.getAnswer();
                List s = new LinkedList();
                LinkedHashMap<String, String> jsonmap = JSON.parseObject(answer, new TypeReference<LinkedHashMap<String, String>>() {
                });
                for (Map.Entry<String, String> entry : jsonmap.entrySet()) {
                    s.add(entry.getValue());
                }
                JSONObject jso = new JSONObject(true);
                jso.put("text", s.toArray());
                sa.setAnswer(jso.toJSONString());
            }
            if (Validator.isEmpty(stu.getActualScore()) || stu.getActualScore() == 0) {
                sa.setFlag(false);
            } else if (stu.getActualScore() > 0) {
                sa.setFlag(true);
            }
            sa.setCostTime(one.getCostTime());
            sa.setTotalScore(one.getScore());
            // 增加是否收藏逻辑
            QueryWrapper<CollectionQuestion> collectionQuestionQueryWrapper = new QueryWrapper<>();
            collectionQuestionQueryWrapper.eq("user_id", sa.getStudentId());
            collectionQuestionQueryWrapper.eq("question_id", sa.getQuestionId());
            CollectionQuestion collectionQuestion = collectionQuestionService.getOne(collectionQuestionQueryWrapper);
            long collectionId = collectionQuestion == null ? 0l : collectionQuestion.getId();
            sa.setCollectionId(collectionId);
            ll.add(sa);
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取用户考试信息成功。。。", ll);
    }

    /**
     * 判断学生提交每一道客观试题答案是否正确
     */
    @RequestMapping(value = "judgePerQuestion")
    public Map judgePerQuestion(@RequestParam("stuAnswer") String stuAnswer, @RequestParam("questionId") Long questionId) {
        boolean flag = false;
        Map<String, Object> map = new HashMap<>();
        try {
            Question question = questionService.getById(questionId);
            String answer = question.getAnswer();
            JSONObject jsonObject = JSONObject.parseObject(answer);
            String qAnswer = jsonObject.getString("text");
            if (question.getType() == 1 || question.getType() == 3) {
                //单选 判断
                if (qAnswer.equals(stuAnswer)) {
                    flag = true;
                }
            } else if (question.getType() == 2) {
                //多选
                List sList = new ArrayList();
                List qList = new ArrayList();
                String[] s = qAnswer.split(",");
                String[] q = stuAnswer.split(",");
                for (int i = 0; i < s.length; i++) {
                    sList.add(s[i]);
                }
                for (int i = 0; i < q.length; i++) {
                    qList.add(q[i]);
                }
                Collections.sort(sList);
                Collections.sort(qList);
                String join1 = String.join(",", sList);
                String join2 = String.join(",", qList);
                if (join1.equals(join2)) {
                    flag = true;
                }
            }
            map.put("flag", flag);
            map.put("qAnswer", qAnswer);
            map.put("sAnswer", stuAnswer);
        } catch (Exception e) {
            map.put("flag", flag);
            map.put("qAnswer", "");
            map.put("sAnswer", "");
            logger.error("judgePerQuestion判断学生提交答案失败:", e);
        }
        return map;
    }

    /**
     * 强制交卷
     *
     * @param identityCard
     * @return
     */
    @ApiOperation(value = "强制交卷")
    @ApiImplicitParam(name = "identityCard", value = "准考证号", dataType = "String")
    @RequestMapping(value = "/forceFinishExam", method = RequestMethod.GET)
    public ApiResult forceFinishExam(String identityCard) {
        QueryWrapper<DrawResult> qw = new QueryWrapper();
        qw.eq("identity_card", identityCard);
        DrawResult one = drawResultService.getOne(qw);
        if (ObjectUtil.isEmpty(identityCard) || ObjectUtil.isEmpty(one)) {
            throw new IllegalArgumentException("无效的准考证号。");
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "强制交卷。。。", null);
    }

}
