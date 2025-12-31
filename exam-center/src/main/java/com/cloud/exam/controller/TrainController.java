package com.cloud.exam.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.core.ExamConstants;
import com.cloud.exam.dao.ErrorQuestionDao;
import com.cloud.exam.model.exam.*;
import com.cloud.exam.service.*;
import com.cloud.exam.utils.CommonPar;
import com.cloud.exam.utils.PageBean;
import com.cloud.exam.utils.exam.QuestionUtils;
import com.cloud.exam.vo.DrawResultVO;
import com.cloud.exam.vo.QuestionVO;
import com.cloud.exam.vo.RuleBeanVO;
import com.cloud.exception.ResultMesCode;
import com.cloud.feign.model.ModelFeign;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.user.AppUser;
import com.cloud.model.user.LoginAppUser;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.utils.DateConvertUtils;
import com.cloud.utils.StringUtils;
import com.cloud.utils.Validator;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by dyl on 2021/11/16.
 */
@RestController
@RefreshScope
public class TrainController {

    @Autowired
    private ExamService examService;
    @Autowired
    private PaperService paperService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private SysDepartmentFeign sysDepartmentFeign;
    @Autowired
    private DrawResultService drawResultService;
    @Autowired
    private TrainService trainService;
    @Autowired
    private StudentAnswerService studentAnswerService;
    @Autowired
    private QuestionKpRelService questionKpRelService;
    @Autowired
    private ModelFeign modelFeign;
    private static final Logger logger = LoggerFactory.getLogger(TrainController.class);
    @Autowired
    private ErrorQuestionDao errorQuestionDao;
    @Autowired
    CollectionQuestionService collectionQuestionService;

    @ApiOperation(value = "获取用户所有训练")
    @RequestMapping(value = "/getUserTrainList", method = RequestMethod.POST)
    public ApiResult getUserTrainList(@RequestBody HashMap<String, Object> map) {
        Object name = map.get("name");
        Object size = map.get("size");
        Object page = map.get("page");
        //List<String> ld =(List<String>) map.get("type");

        List<Integer> type = (List<Integer>) map.get("type");

        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        Page<Exam> pg = new Page<>(Integer.valueOf(String.valueOf(page)), Integer.valueOf(String.valueOf(size)));
        QueryWrapper<Exam> entityWrapper = new QueryWrapper<>();

        QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginAppUser.getId());
        List<DrawResult> list = drawResultService.list(queryWrapper);
        List<Long> ll = new ArrayList<>();
        list.stream().forEach(e -> ll.add(e.getAcId()));

        /*if(CollectionUtils.isNotEmpty(ll)){
            entityWrapper.and(Wrapper->Wrapper.in("id",ll).or().eq("creator",loginAppUser.getId()));
        }else {
            entityWrapper.eq("creator",loginAppUser.getId());
        }*/
        ll.add(0L);
        entityWrapper.in("id", ll);
        if (CollectionUtils.isEmpty(type)) {
            entityWrapper.eq("type", null);
        } else {
            entityWrapper.in("type", type);
        }

        if (ObjectUtil.isNotNull(name) && StringUtils.isNotBlank(name.toString())) {
            entityWrapper.like("name", name);
        }
        List ls = new ArrayList();
        ls.add(ExamConstants.EXAM_NOT_LOGIN);
        ls.add(ExamConstants.ACTIVITY_LAUNCH);
        ls.add(ExamConstants.ACTIVITY_START);
        ls.add(ExamConstants.EXAM_START);
        entityWrapper.in("exam_status", ls);
        entityWrapper.orderByAsc("start_time");
        /*Set<SysRole> rolesByUserId = sysDepartmentFeign.findRolesByUserId(loginAppUser.getId());
        boolean b = rolesByUserId.stream().anyMatch(e -> ExamConstants.SYS_SUPER_ADMIN.equals(e.getId()));*/

        IPage<Exam> answerVOIPage = examService.page(pg, entityWrapper);
        List<Exam> records = answerVOIPage.getRecords();
        for (Exam exam : records) {
            if (ExamConstants.EXAM_TYPE_XUNLIAN.equals(exam.getType())) {
                AppUser appUserById = sysDepartmentFeign.findAppUserById(exam.getCreator());
                //SysDepartment sysDepartmentById1 = sysDepartmentFeign.findSysDepartmentById(appUserById.getDepartmentId());
                exam.setCreateDepartName(appUserById.getNickname());
                QueryWrapper<DrawResult> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("ac_id", exam.getId());
                queryWrapper1.eq("user_id", loginAppUser.getId());
                DrawResult one = drawResultService.getOne(queryWrapper1);
                Paper paper = paperService.getById(one.getPaperId());
                exam.setIdentityCard(one == null ? "" : one.getIdentityCard());
                exam.setTotalTime(one == null ? 0L : paper.getTotalTime());
                if (exam.getExamStatus().equals(ExamConstants.EXAM_START) && one.getUserStatus().equals(ExamConstants.EXAM_NOT_LOGIN)) {
                    exam.setExamStatus(ExamConstants.EXAM_START);
                } else {
                    exam.setExamStatus(one.getUserStatus());
                }
            } else {
                exam.setCreateDepartName(sysDepartmentFeign.findAppUserById(exam.getCreator()).getNickname());
                QueryWrapper<DrawResult> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("ac_id", exam.getId());
                DrawResult one = drawResultService.getOne(queryWrapper1);
                exam.setIdentityCard(one.getIdentityCard());
                exam.setTotalTime(Long.valueOf(paperService.getById(one.getPaperId()).getTotalTime()));
            }
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), ApiResultHandler.success().getMessage(), answerVOIPage);
    }

    @ApiOperation(value = "用户创建自测")
    @RequestMapping(value = "/createSelfTrain", method = RequestMethod.POST)
    public ApiResult createSelfTrain(@RequestBody RuleBeanVO rule) {

        Boolean aBoolean = trainService.saveSelfTrain(rule);
        if (aBoolean) {
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), ApiResultHandler.success().getMessage(), true);
        } else {
            return ApiResultHandler.otherError(ResultMesCode.INTERNAL_SERVER_ERROR);
        }

    }

    @ApiOperation(value = "查询用户已经结束的训练活动")
    @RequestMapping(value = "/getTrainsByUser", method = RequestMethod.POST)
    public ApiResult getTrainsByUser(@RequestBody Map<Object, Object> map) {
        Integer page = (Integer) map.get("page");
        Integer size = (Integer) map.get("size");
        Object name = map.get("name");
        List<Integer> ls = (List<Integer>) map.get("type");
        Page pg = new Page(page, size);
        Long id = AppUserUtil.getLoginAppUser().getId();
        List<Long> ll = new ArrayList<>();
        QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", id);
        queryWrapper.eq("user_status", ExamConstants.ACTIVITY_FINISH);
        if (ObjectUtil.isNotEmpty(name)) {
            ll.add(0L);
            QueryWrapper<Exam> qw = new QueryWrapper<>();
            qw.like("name", name);
            List<Exam> list = examService.list(qw);
            list.stream().forEach(e -> ll.add(e.getId()));
            queryWrapper.in("ac_id", ll);
        }
        if (CollectionUtils.isNotEmpty(ls)) {
            queryWrapper.in("exam_type", ls);
        } else {
            ls = new ArrayList<>();
            queryWrapper.eq("exam_type", -1);
        }
        if (ls.contains(7) || ls.contains(8) || ls.contains(9) || ls.contains(10) || ls.contains(11)) {
            List<DrawResultVO> drawResultVOList = new ArrayList<>();
            List<DrawResult> drawResultList = drawResultService.list(queryWrapper);
            for (DrawResult d : drawResultList) {
                DrawResultVO vo = new DrawResultVO();
                vo.setUserStatus(d.getUserStatus());
                vo.setCostTime(d.getCostTime());
                vo.setScore(d.getScore());
                Exam byId = examService.getById(d.getAcId());
                vo.setExamName(byId.getName());
                vo.setExamType(byId.getType());
                if (byId.getType().equals(ExamConstants.EXAM_TYPE_XUNLIAN)) {
                    vo.setExamDate(DateConvertUtils.date2Str(examService.getById(d.getAcId()).getStartTime()));
                } else {
                    vo.setExamDate(DateConvertUtils.date2Str(examService.getById(d.getAcId()).getCreateTime()));
                }
                vo.setAcId(d.getAcId());
                vo.setIdentityCard(d.getIdentityCard());
                vo.setPaperType(d.getPaperType());
                drawResultVOList.add(vo);
            }
            List<Long> qIds = new ArrayList<>();
            qIds.add(0L);

            QueryWrapper<Question> qw = new QueryWrapper<>();
            List<Integer> ls1 = Arrays.asList(8, 9, 10, 11, 12);
            qw.in("type", ls1);
            if (ObjectUtil.isNotEmpty(name)) {
                qw.like("question", name);
            }
            List<Question> list = questionService.list(qw);
            list.stream().forEach(e -> qIds.add(e.getId()));
            QueryWrapper<StudentAnswer> qww = new QueryWrapper<>();
            qww.eq("student_id", AppUserUtil.getLoginAppUser().getId());
            qww.in("question_id", qIds);
            List<StudentAnswer> list1 = studentAnswerService.list(qww);
            List<StudentAnswer> collect = list1.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(
                    o -> o.getStudentId() + ";" + o.getQuestionId()))), ArrayList::new)
            );
            for (StudentAnswer st : collect) {
                DrawResultVO vo = new DrawResultVO();
                vo.setExamName(questionService.getById(st.getQuestionId()).getQuestion());
                vo.setExamDate(DateConvertUtils.date2Str(st.getCreateTime()));
                vo.setPaperType(questionService.getById(st.getQuestionId()).getType());
                // 增加是否收藏逻辑
                QueryWrapper<CollectionQuestion> collectionQuestionQueryWrapper = new QueryWrapper<>();
                collectionQuestionQueryWrapper.eq("user_id", st.getStudentId());
                collectionQuestionQueryWrapper.eq("question_id", st.getQuestionId());
                CollectionQuestion collectionQuestion = collectionQuestionService.getOne(collectionQuestionQueryWrapper);
                long collectionId = collectionQuestion == null ? 0L : collectionQuestion.getId();
                vo.setCollectionId(collectionId);
                vo.setQuestionId(st.getQuestionId());
                drawResultVOList.add(vo);
            }
            //根据size分成j页
            //int j = (drawResultVOList.size() + size - 1) / size;
            List<DrawResultVO> collect1 = drawResultVOList.stream().skip((page - 1) * size).limit(size).collect(Collectors.toList());
            PageBean pageBean1 = PageBean.getPageBean(page, size, drawResultVOList.size(), collect1);

            return ApiResultHandler.buildApiResult(200, "查询成功", pageBean1);
        } else {

            IPage page1 = drawResultService.page(pg, queryWrapper);

            page1.convert(dr -> {
                DrawResult d = (DrawResult) dr;
                DrawResultVO vo = new DrawResultVO();
                vo.setUserStatus(d.getUserStatus());
                vo.setCostTime(d.getCostTime());
                vo.setScore(d.getScore());
                Exam byId = examService.getById(d.getAcId());
                vo.setExamName(byId.getName());
                vo.setExamType(byId.getType());
                if (byId.getType().equals(ExamConstants.EXAM_TYPE_XUNLIAN)) {
                    vo.setExamDate(DateConvertUtils.date2Str(examService.getById(d.getAcId()).getStartTime()));
                } else {
                    vo.setExamDate(DateConvertUtils.date2Str(examService.getById(d.getAcId()).getCreateTime()));
                }
                vo.setAcId(d.getAcId());
                vo.setIdentityCard(d.getIdentityCard());
                vo.setPaperType(d.getPaperType());
                return vo;
            });
            return ApiResultHandler.buildApiResult(200, "查询成功", page1);
        }

    }


    @ApiOperation(value = "查询所有训练活动", notes = "查询所有主活动")
    @RequestMapping(value = "/getTrainList", method = RequestMethod.GET)
    public ApiResult getTrainList(String name, String examStatus, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") Date startTime, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") Date endTime, Integer page, Integer size) {
        Page<Exam> pg = new Page<>(page, size);
        QueryWrapper<Exam> entityWrapper = new QueryWrapper<>();

        if (Validator.isNotNull(name)) {
            entityWrapper.like("name", name);
        }
        if (!Validator.isEmpty(startTime)) {
            entityWrapper.ge("start_time", startTime);
        }
        if (!Validator.isEmpty(endTime)) {
            entityWrapper.le("end_time", endTime);
        }
        if (Validator.isNotNull(examStatus)) {
            List ll = new ArrayList();
            String[] split = examStatus.split(",");
            for (int i = 0; i < split.length; i++) {
                ll.add(Integer.valueOf(split[i]));
            }
            entityWrapper.in("exam_status", ll);
        }
        entityWrapper.eq("type", ExamConstants.EXAM_TYPE_XUNLIAN);
        entityWrapper.orderByDesc("create_time");
        IPage<Exam> answerVOIPage = examService.page(pg, entityWrapper);
        for (Exam am : answerVOIPage.getRecords()) {
            Long creatorId = am.getCreator();
            AppUser appUserById = sysDepartmentFeign.findAppUserById(creatorId);
            am.setCreateDepartName(appUserById.getDepartmentName());
            am.setTotalTime(DateConvertUtils.getMinOfTime(am.getStartTime(), am.getEndTime()));
        }
        return ApiResultHandler.buildApiResult(200, "查询成功", answerVOIPage);
    }

    /**
     * 判断考生连线题答案否正确
     *
     * @param questionId
     * @param identityId
     * @return
     */
    @RequestMapping(value = "getRelationAnswer", method = RequestMethod.GET)
    public ApiResult getRelationAnswer(Long questionId, String identityId) {
        QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("identity_card", identityId);
        DrawResult drawResult = drawResultService.getOne(queryWrapper);
        QueryWrapper<StudentAnswer> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("question_id", questionId);
        queryWrapper1.eq("student_id", drawResult.getUserId());
        queryWrapper1.eq("paper_id", drawResult.getPaperId());
        StudentAnswer studentAnswer = studentAnswerService.getOne(queryWrapper1);
        Question question = questionService.getById(questionId);
        QuestionVO vo = new QuestionVO();
        String answer = question.getAnswer();
        vo.setAnswer(question.getAnswer());
        vo.setAnalysis(question.getAnalysis());
        if (ObjectUtil.isEmpty(studentAnswer)) {
            vo.setJudge(false);
        } else {
            String stuAnswer = studentAnswer.getStuAnswer();
            JSONArray sanser = JSONArray.parseArray(stuAnswer);
            JSONArray qanser = JSONArray.parseArray(answer);
            boolean b = judgeRelationsAnswer(sanser, qanser);
            vo.setAnalysis(question.getAnalysis());
            vo.setAnswer(question.getAnswer());
            vo.setJudge(b);
        }
        return ApiResultHandler.buildApiResult(200, "查询成功", vo);
    }

    /**
     * 判断考生答案否正确(标注题 连线题 整编题 单选 多选 判断 填空)
     *
     * @param map
     * @return
     */
    @ApiOperation(value = "判断答案")
    @RequestMapping(value = "judgeStudentRelationAnswer", method = RequestMethod.POST)
    public ApiResult judgeStudentRelationAnswer(@RequestBody Map<String, Object> map) {
        Object questionId = map.get("questionId");
        Object studentAnswer = map.get("studentAnswer");
        Question question = questionService.getById(Long.valueOf(questionId + ""));
        QuestionVO vo = new QuestionVO();
        String answer = question.getAnswer();
        vo.setAnswer(question.getAnswer());
        vo.setAnalysis(question.getAnalysis());
        vo.setQuestion(question.getQuestion());
        vo.setOptions(question.getOptions());
        vo.setStuAnswer(studentAnswer + "");
        vo.setType(question.getType());

        if (question.getType() == 9) {
            if (ObjectUtil.isEmpty(studentAnswer) || (studentAnswer + "").contains("text")) {
                vo.setJudge(false);
            } else {
                JSONArray sanser = JSONArray.parseArray(studentAnswer + "");
                JSONArray qanser = JSONArray.parseArray(answer);
                boolean b = judgeRelationsAnswer(sanser, qanser);
                vo.setJudge(b);
            }
        }
        // 添加错题收录逻辑
        if (question.getType() == 9 && !vo.getJudge()) {
            LoginAppUser loginAppUser = sysDepartmentFeign.getLoginAppUser();
            Map<String, Object> parMap = new HashMap<>();
            // 根据用户id和试题id查询是否已记录
            parMap.put("userId", loginAppUser.getId());
            parMap.put("questionId", questionId);
            List<Map<String, Object>> byPar = errorQuestionDao.findByPar(parMap);
//            byPar = CollectionsCustomer.builder().build().listMapToLowerCase(byPar);
            if (byPar == null || byPar.size() == 0) {
                parMap.put("question", question.getQuestion());
                parMap.put("type", CommonPar.question_type.get(question.getType().toString()));
                parMap.put("createTime", new Timestamp(System.currentTimeMillis()));
                errorQuestionDao.saveInfo(parMap);
            } else {
                parMap.put("id", Long.valueOf(byPar.get(0).get("id").toString()));
                parMap.put("createTime", new Timestamp(System.currentTimeMillis()));
                errorQuestionDao.updateInfo(parMap);
            }
        }

        /*QueryWrapper<StudentAnswer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id",AppUserUtil.getLoginAppUser().getId());
        queryWrapper.eq("question_id",questionId);
        List<StudentAnswer> list = studentAnswerService.list(queryWrapper);*/
        //将答案存入studentAnswer表中
        StudentAnswer studentAnswer1 = new StudentAnswer();
        studentAnswer1.setCreateTime(new Date());
        studentAnswer1.setQuestionId(Long.valueOf(questionId + ""));
        studentAnswer1.setPaperId(0L);
        studentAnswer1.setPaperType(-1);
        studentAnswer1.setStuAnswer(studentAnswer + "");
        studentAnswer1.setType(question.getType());
        studentAnswer1.setStudentId(AppUserUtil.getLoginAppUser().getId());
        studentAnswer1.setPdType(questionService.getById(studentAnswer1.getQuestionId()).getPdType());
        studentAnswerService.save(studentAnswer1);
        return ApiResultHandler.buildApiResult(200, "查询成功", vo);
    }


    public boolean judgeRelationsAnswer(JSONArray sanser, JSONArray qanser) {
        try {
            if (sanser.size() * qanser.size() == 0 || sanser.size() != qanser.size()) {
                return false;
            } else {
                Map<String, List<String>> m1 = new HashMap<>();
                Map<String, List<String>> m2 = new HashMap<>();
                for (int i = 0; i < sanser.size(); i++) {
                    List<String> strings = (List<String>) sanser.get(i);
                    if (m1.containsKey(strings.get(0))) {
                        m1.get(strings.get(0)).add(strings.get(1));
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(strings.get(1));
                        m1.put(strings.get(0), list);
                    }
                }

                for (int i = 0; i < qanser.size(); i++) {
                    List<String> strings = (List<String>) qanser.get(i);
                    if (m2.containsKey(strings.get(0))) {
                        m2.get(strings.get(0)).add(strings.get(1));
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(strings.get(1));
                        m2.put(strings.get(0), list);
                    }
                }
                for (Map.Entry<String, List<String>> map : m1.entrySet()) {
                    String key = map.getKey();
                    List<String> value = map.getValue();
                    if (!m2.containsKey(key)) {
                        return false;
                    } else {
                        List<String> list = m2.get(key);
                        if (!value.stream().sorted().collect(Collectors.joining()).equals(list.stream().sorted().collect(Collectors.joining()))) {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("判断考生连线题答案失败：{}", e.getMessage());
            return false;
        }
        return true;
    }

    @Autowired
    QuestionManageService questionManageService;

    /**
     * 获取解疑训练列表
     */
    @ApiOperation(value = "获取解疑训练列表")
    @RequestMapping(value = "getAnalysisQuestionList", method = RequestMethod.GET)
    public ApiResult getAnalysisQuestionList(String name, String types, Integer page, Integer size) {
        Page<QuestionManage> pg = new Page<>(page, size);
        QueryWrapper<QuestionManage> questionQueryWrapper = new QueryWrapper<>();
        if (ObjectUtil.isNotEmpty(name)) {
            questionQueryWrapper.like("question", name);
        }
        List<Integer> ll = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(types)) {
            String[] split = types.split(",");
            List<String> strings = Arrays.asList(split);
            strings.stream().forEach(e -> ll.add(Integer.parseInt(e)));
            questionQueryWrapper.in("type", ll);
        } else {
            ll.add(0);
            questionQueryWrapper.in("type", ll);
        }
        questionQueryWrapper.orderByDesc("create_time");
        IPage<QuestionManage> list = questionManageService.page(pg, questionQueryWrapper);
        for (QuestionManage e : list.getRecords()) {
            e.setAnswer(null);
            e.setAnalysis(null);
            String kpnameByQuestionId = QuestionUtils.getKpNamesByQuestionManage(e);
            e.setKpName(kpnameByQuestionId);
        }

        return ApiResultHandler.buildApiResult(200, "查询成功", list);
    }

    /**
     * 获取解释训练中相关的知识
     *
     * @param questionId
     * @return
     */
    @ApiOperation("获取解释训练知识")
    @RequestMapping(value = "listKnowledgePageByQuestion", method = RequestMethod.GET)
    public ApiResult listKnowledgePageByQuestion(Long questionId, Integer page, Integer size) {
        Set<String> kpIdList = sysDepartmentFeign.getKpIdsbyUserId(AppUserUtil.getLoginAppUser().getId());
        QueryWrapper<QuestionKpRel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("question_id", questionId);
        List<QuestionKpRel> list1 = questionKpRelService.list(queryWrapper);
        Set<String> set = new HashSet();
        list1.stream().forEach(e -> set.add(e.getKpId()));
        Long id = AppUserUtil.getLoginAppUser().getId();
        List<Map> ll = new ArrayList<>();
        String kpIds = set.stream().map(String::valueOf).collect(Collectors.joining(","));
        Map map = modelFeign.listKnowledgePage(1, 100, Integer.parseInt(id + ""), kpIds, "");
//        map = CollectionsCustomer.builder().build().mapToLowerCase(map);

        Map<String, Object> data = (Map<String, Object>) map.get("data");
        Map<String, Object> res = (Map<String, Object>) data.get("result");
        Map mp = new HashMap();
        mp.put("current", page);
        mp.put("size", size);
        if (ObjectUtil.isNull(res)) {
            mp.put("pages", 0);
            mp.put("total", 0);
            mp.put("records", new ArrayList<>());
        } else {
            List<Map> list = (List) res.get("records");
            for (Map m : list) {
                if (kpIdList.contains(m.get("kpId"))) {
                    ll.add(m);
                }
            }
            int pages = ll.size() % size == 0 ? ll.size() / size : (ll.size() / size) + 1;
            mp.put("pages", pages);
            mp.put("total", ll.size());
            if (size * (page - 1) >= ll.size()) {
                mp.put("records", new ArrayList<>());
            } else {
                mp.put("records", ll.subList(size * (page - 1), (size * page > ll.size() ? ll.size() : (size * page))));
            }
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取成功。。。", mp);
    }

    /**
     * 查询相关的训练
     */
    @RequestMapping(value = "listTrainListByQuestion", method = RequestMethod.GET)
    public ApiResult listTrainListByQuestion(Long questionId, Integer page, Integer size) {
        QueryWrapper<QuestionKpRel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("question_id", questionId);
        List<QuestionKpRel> list1 = questionKpRelService.list(queryWrapper);
        Set<String> set = new HashSet();
        set.add("0");
        list1.stream().forEach(e -> set.add(e.getKpId()));
        Set<Long> list = new HashSet<>();
        QueryWrapper<QuestionKpRel> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.in("kp_id", set);
        List<QuestionKpRel> list2 = questionKpRelService.list(queryWrapper1);
        list2.stream().forEach(e -> list.add(e.getQuestionId()));
        List<Question> questionList = new ArrayList<>();
        list.parallelStream().forEach(e -> {
            Question byId = questionService.getById(e);
            if (ObjectUtil.isNotNull(byId)) {
                if (byId.getType() == 7 || byId.getType() == 8 || byId.getType() == 9) {
                    questionList.add(questionService.getById(e));
                }
            }
        });
        questionList.removeIf(q -> q.getId().equals(questionId));
        //根据size分成j页
        List<Question> collect1 = questionList.stream().skip((page - 1) * size).limit(size).collect(Collectors.toList());
        PageBean pageBean1 = PageBean.getPageBean(page, size, questionList.size(), collect1);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取成功。。。", pageBean1);
    }
}
