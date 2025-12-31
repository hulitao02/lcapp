package com.cloud.exam.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.core.ExamConstants;
import com.cloud.exam.dao.ErrorQuestionDao;
import com.cloud.exam.model.exam.*;
import com.cloud.exam.model.record.Record;
import com.cloud.exam.service.*;
import com.cloud.exam.utils.CommonPar;
import com.cloud.exam.utils.SpiltQuestionUtils;
import com.cloud.exam.utils.Tools;
import com.cloud.exam.vo.DrawResultVO;
import com.cloud.exam.vo.QuestionVO;
import com.cloud.exam.vo.StudentAnswerVO;
import com.cloud.exception.ResultMesCode;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.user.LoginAppUser;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.utils.RedisUtils;
import com.cloud.utils.Validator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dyl on 2021/06/22.
 * 竞答活动答题处理类
 */
@Api(value = "竞答活动答题处理类")
@RestController
@RequestMapping("/competitionquestion")
public class CompetitionQuestionController {

    @Resource
    private CompetitionExamPaperRelService competitionExamPaperRelService;
    @Resource
    private PaperManageRelService paperManageRelService;
    @Resource
    private QuestionService questionService;
    @Resource
    private ExamService examService;
    @Resource
    private DrawResultService drawResultService;
    @Resource
    private StudentAnswerService studentAnswerService;
    @Resource
    private PaperService paperService;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private SysDepartmentFeign sysDepartmentFeign;

    @Resource
    private RecordService recordService;
    @Resource
    ErrorQuestionDao errorQuestionDao;
    @Resource
    private ExamKpPersonAvgScoreService examKpPersonAvgScoreService;


    @ApiOperation(value = "获取每个试卷的试题")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "examId", value = "活动id", dataType = "Long"),
            @ApiImplicitParam(name = "paperId", value = "试卷id", dataType = "Long")
    })
    @RequestMapping("/getQuestionByPaperId")
    public ApiResult getQuestionByPaperId(Long examId, Long paperId) {
        /* QueryWrapper queryWrapper1 = new QueryWrapper();
        queryWrapper1.eq("paper_id",paperId);
        List<StudentAnswer> list2 = studentAnswerService.list(queryWrapper1);
        Exam exam = examService.getById(examId);
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        boolean ishost = false;
        if(exam.getHostId().equals(loginAppUser.getId())){
            ishost = true;
        }*/

        /*QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("paper_id",paperId);
        List<PaperManageRel> list = paperManageRelService.list(queryWrapper);
        List<QuestionVO> ll = new ArrayList<>();
        for (PaperManageRel pmr:list) {
            Question byId = questionService.getById(pmr.getQuestionId());
            QuestionVO  vo = new QuestionVO();
            BeanUtils.copyProperties(byId,vo);
            vo.setQuestionScore(pmr.getScore());
            vo.setQuestionSort(pmr.getSort());
            vo.setRelId(pmr.getId());
            vo.setPaperType(paper.getType());
            vo.setType(byId.getType());
            vo.setQuestionTime(pmr.getQuestionTime());
            //根据不同用户选择是否返回试题答案和试题解析
            *//*if(!ishost){
                vo.setAnswer(null);
                vo.setAnalysis(null);
            }*//*
            if(paper.getType()==6){
                //选答试卷,判断试题是否被占用
                boolean b = list2.stream().anyMatch(stu -> stu.getQuestionId().equals(byId.getId()));
                if (b){
                    vo.setCheck(true);
                }
            }
            ll.add(vo);
        }
        Collections.sort(ll, new Comparator<QuestionVO>() {
            @Override
            public int compare(QuestionVO o1, QuestionVO o2) {
                return o1.getType() - o2.getType();
            }
        });
        Map<Integer, List<QuestionVO>> collect = ll.stream().collect(Collectors.groupingBy(QuestionVO::getType));
        for (Integer i:collect.keySet()) {
            List<QuestionVO> list1 = collect.get(i);
            Collections.sort(list1, new Comparator<QuestionVO>() {
                @Override
                public int compare(QuestionVO o1, QuestionVO o2) {
                    return o1.getQuestionSort()-o2.getQuestionSort();
                }
            });
        }*/
        Paper paper = paperService.getById(paperId);
        List<QuestionVO>  ll = getAllQuestionByPaperId(paperId);
        if(paper.getType()==4){
            //获取分组
            List<Object> objects = redisUtils.lGet("competition:group:" + examId, 0, -1);
            if(!Validator.isEmpty(objects) && objects.size()>0){
                List<DrawResult> ls = (List<DrawResult>)objects.get(0);
                //试题id-->对应答题人员id
                HashMap<String, Object> longListHashMap = SpiltQuestionUtils.spiltQuestion(ls, ll);
                //轮答方式，保存必答试卷的话把每个小组对应的必答试题对应信息保存到redis
                redisUtils.delKeys("competition:question:"+examId+":"+paperId);
                redisUtils.hmset("competition:question:"+examId+":"+paperId,longListHashMap,ExamConstants.EXAM_OTHER_DETAILS_TIME7);
            }
        }
        /*if(paper.getType()==4 || paper.getType()==5){
            List<QuestionVO> collect = ll.stream().sorted(Comparator.comparing(QuestionVO::getCheck).reversed()).collect(Collectors.toList());
        }*/
        //保存每个试卷对应的试题
        redisUtils.delKeys("competition:"+examId+":"+paperId);
        redisUtils.lSet("competition:"+examId+":"+paperId,ll, ExamConstants.EXAM_PAPER_QUESTION_DETAILS_TIME);
        return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),ResultMesCode.SUCCESS.getResultMsg(),ll);
    }

    /**
     * 向试卷添加试题
     * 添加的试题分数无法获取
     * @param paperId
     * @return
     */
    @ApiOperation(value = "向试卷添加试题")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "questionId",value = "试题id",dataType = "Long"),
            @ApiImplicitParam(name = "paperId",value = "试卷id",dataType = "Long")
    })
    @RequestMapping("/addQuestion2Paper")
    public ApiResult addQuestion2Paper(@RequestParam  Long paperId,@RequestParam  Long questionId){
        Question question = questionService.getById(questionId);
        QueryWrapper queryWrapper1 = new QueryWrapper();
        queryWrapper1.eq("paper_id",paperId);
        List<PaperManageRel>  list = paperManageRelService.list(queryWrapper1);
        PaperManageRel pmr = new PaperManageRel();
        pmr.setQuestionId(questionId);
        pmr.setPaperId(paperId);
        pmr.setScoreBasis(question.getScoreBasis());
        pmr.setSort(list.size()+1);
        return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),ResultMesCode.SUCCESS.getResultMsg(),null);
    }
    @ApiOperation(value = "获取活动下的分组")
    @ApiImplicitParam(name = "examId",value = "活动id",required = true,dataType = "Long")
    @RequestMapping(value = "/getMembersByExamId",method = RequestMethod.POST)
    public ApiResult getMembersByExamId(@RequestParam Long examId){
        QueryWrapper qw = new QueryWrapper();
        qw.eq("ac_id",examId);
        Exam exam = examService.getById(examId);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("ac_id",examId);
        List<DrawResult> list = drawResultService.list(queryWrapper);
        List<DrawResultVO>  ll = new ArrayList<>();
        if(exam.getUnit()==0){
            //团体竞答,每个单位做为一个分组
            Map<Long, List<DrawResult>> collect = list.stream().collect(Collectors.groupingBy(DrawResult::getDepartId));
            list.clear();
            for (Long departId:collect.keySet()) {
                list.add(collect.get(departId).get(0));
            }
        }
        list.sort((d1,d2)->Integer.valueOf(d1.getPlaceNum())-Integer.valueOf(d2.getPlaceNum()));
        for (DrawResult dr:list) {
            DrawResultVO vo = new DrawResultVO();
            BeanUtils.copyProperties(dr,vo);
            if(exam.getUnit()==0){
                //团体竞答
                String departName = sysDepartmentFeign.findSysDepartmentById(dr.getDepartId()).getDname();
                vo.setGroupName(departName);
            }else {
                String nickname = sysDepartmentFeign.findAppUserById(dr.getUserId()).getNickname();
                vo.setGroupName(nickname);
            }
            //vo.setGroupName("第"+ ChangeInt2chn.tranInt(Integer.valueOf(dr.getPlaceNum()))+"组");
            ll.add(vo);
            Tools.putCompetitionGroupCache(examId+"&"+vo.getPlaceNum(),vo.getGroupName());
            Tools.putCompetitionScoreCache(examId+"&"+vo.getPlaceNum(),vo.getScore());
        }
        //保存活动分组
        redisUtils.delKeys("competition:group:"+examId);
        redisUtils.lSet("competition:group:"+examId,list);
        /*Object submitquestion = Tools.getJudgeCache("submitquestion");
        if(!Validator.isEmpty(submitquestion)){
            HashMap  map = (HashMap)submitquestion;
            Object placenum = map.get("placenum");
            Object stuanswer = map.get("stuanswer");
            ll.stream().forEach(e->{
                if(e.getPlaceNum().equals(placenum)){
                    e.setStuAnswers(stuanswer);
                }
            });
        }*/
        return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),ResultMesCode.SUCCESS.getResultMsg(),ll);
    }

    @ApiOperation(value = "考生点击提交答案/倒计时结束自动提交答案")
    @RequestMapping(value = "/saveStudentQuestionAnswer",method = RequestMethod.POST)
    public synchronized ApiResult saveStudentQuestionAnswer(@RequestBody StudentAnswerVO sa){
        Long examId = sa.getExamId();
        Long questionId = sa.getQuestionId();
        Long paperId = sa.getPaperId();
        QueryWrapper<StudentAnswer> qwe = new QueryWrapper<>();
        qwe.eq("question_id",questionId);
        qwe.eq("paper_id",paperId);
        List<StudentAnswer> list = studentAnswerService.list(qwe);

        if(list!=null && list.size()>0){
           /* list.stream().forEach(e->{
                studentAnswerService.removeById(e.getId());
            });*/
            throw new IllegalArgumentException("答案已提交，请不要重复提交。。。");
        }

        StudentAnswer studentAnswer = new StudentAnswer();
        studentAnswer.setQuestionId(sa.getQuestionId());
        Exam exam = examService.getById(sa.getExamId());
        Integer ss = exam.getScoringMethod();
        JSONObject  jsonObject = new JSONObject();
        if(sa.getType()==2){
            if(Validator.isEmpty(sa.getStuAnswers())){
                List<String> ll = new ArrayList<>();
                jsonObject.put("text",ll);
            }else{
                List<String> ll = (List<String>)sa.getStuAnswers();
                jsonObject.put("text",ll);
            }
        }else{
            if(Validator.isEmpty(sa.getStuAnswers())){
                jsonObject.put("text","");
            }else{
                jsonObject.put("text",sa.getStuAnswers());
            }
        }
        studentAnswer.setStuAnswer(jsonObject.toJSONString());
        studentAnswer.setPaperId(sa.getPaperId());
        studentAnswer.setPaperType(sa.getPaperType());
        studentAnswer.setCreateTime(new Date());
        studentAnswer.setType(sa.getType());

        studentAnswer.setStudentId(AppUserUtil.getLoginAppUser().getId());
        Question byId = questionService.getById(sa.getQuestionId());
        boolean isCorrect  =  false;

        String stuAnswer = studentAnswer.getStuAnswer();
        JSONObject jsonObject2 = JSONObject.parseObject(stuAnswer);
        if(Validator.isNotNull(stuAnswer)){
            isCorrect = judgeAnswer(jsonObject2, byId);
        }
        if(isCorrect){
            studentAnswer.setActualScore(sa.getQuestionScore());
        }

        QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ac_id",exam.getId());
        queryWrapper.eq("user_id",AppUserUtil.getLoginAppUser().getId());
        DrawResult drawResult = drawResultService.getOne(queryWrapper);
        studentAnswer.setPlaceNum(drawResult.getPlaceNum());
        studentAnswer.setAcId(examId);
        studentAnswer.setPdType(byId.getPdType());
        studentAnswerService.save(studentAnswer);

        if(isCorrect){
            studentAnswer.setActualScore(sa.getQuestionScore());
            if(exam.getUnit()==0){
                //集体考试
                String placeNum = drawResult.getPlaceNum();
                QueryWrapper<DrawResult> qw = new QueryWrapper<>();
                qw.eq("ac_id",exam.getId());
                qw.eq("place_num",placeNum);
                List<DrawResult>  ls = drawResultService.list(qw);
                //更新集体每个人的分数
                for (DrawResult dr:ls) {
                    dr.setScore(drawResult.getScore()+sa.getQuestionScore());
                    drawResultService.saveOrUpdate(dr);
                }
            }else{
                //更新总得分
                drawResult.setScore(drawResult.getScore()+sa.getQuestionScore());
                drawResultService.saveOrUpdate(drawResult);
            }
        }else{
            if(ss == 0){
                //答错减分
                if(exam.getUnit()==0){
                    //集体考试
                    String placeNum = drawResult.getPlaceNum();
                    QueryWrapper<DrawResult> qw = new QueryWrapper<>();
                    qw.eq("ac_id",exam.getId());
                    qw.eq("place_num",placeNum);
                    List<DrawResult>  ls = drawResultService.list(qw);
                    //更新集体每个人的分数
                    for (DrawResult dr:ls) {
                        dr.setScore(drawResult.getScore()-sa.getQuestionScore()<0?0:drawResult.getScore()-sa.getQuestionScore());
                        drawResultService.saveOrUpdate(dr);
                    }
                }else{
                    //更新总得分
                    drawResult.setScore(drawResult.getScore()-sa.getQuestionScore()<0?0:drawResult.getScore()-sa.getQuestionScore());
                    drawResultService.saveOrUpdate(drawResult);
                }
            }
        }
        //redis 记录当前试卷当前题的下标
        List<QuestionVO>  ll = (List<QuestionVO>)Tools.getJudgeCache(sa.getPaperId());
        Paper paper = paperService.getById(sa.getPaperId());
        if(paper.getType()!=6){
            for(int i = 0;i<ll.size();i++){
                if(ll.get(i).getId().equals(sa.getQuestionId())){
                    Tools.putJudgeCache("paper:"+sa.getPaperId(),i);
                }
            }
        }
        Tools.putCompetitionScoreCache(examId+"&"+drawResult.getPlaceNum(),drawResult.getScore());

        return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),ResultMesCode.SUCCESS.getResultMsg(),null);
    }
    @ApiOperation(value = "主持人点击开始答题")
    @ApiImplicitParam(name = "qIndex", value = "试题或者小组下标", dataType = "Integer")
    @RequestMapping(value = "/startQuestionByhost",method = RequestMethod.GET)
    public ApiResult startQuestionByhost(Long examId,Long paperId,Integer qIndex){
        QueryWrapper<CompetitionExamPaperRel> qwe =  new QueryWrapper();
        qwe.eq("ac_id",examId);
        qwe.eq("paper_id",paperId);
        CompetitionExamPaperRel competitionExamPaperRel = competitionExamPaperRelService.getOne(qwe);
        List<Object> objects = redisUtils.lGet("competition:" + examId + ":" + paperId, 0, -1);
        List<QuestionVO> list = (List<QuestionVO>)objects.get(0);
        Exam exam = examService.getById(examId);
        List<DrawResult> ll = new ArrayList<>();
        Long userId = null;
        if(competitionExamPaperRel.getPaperType()==4){
            //map  --> <String,object>  每组人员--每个试题
            HashMap<Object,Object> map = (HashMap<Object,Object>)redisUtils.hmget("competition:question:"+examId+":"+paperId);
            Object o = map.get(String.valueOf(list.get(qIndex).getId()));
            userId = Long.valueOf(String.valueOf(o));
            //记录当前试题的下标
            redisUtils.set("competition:questionIndex:"+examId+":"+paperId,qIndex,ExamConstants.EXAM_OTHER_DETAILS_TIME7);
        }else if(competitionExamPaperRel.getPaperType()==6){
            List<Object>   objects1 =redisUtils.lGet("competition:group:" + examId, 0, -1);
            List<DrawResult> o =(List<DrawResult> )objects1.get(0);
            DrawResult dr = o.get(qIndex);
            userId = dr.getUserId();
        }else if(competitionExamPaperRel.getPaperType()==5){
            //抢答 记录当前试题的下标
            redisUtils.set("competition:questionIndex:"+examId+":"+paperId,qIndex,ExamConstants.EXAM_OTHER_DETAILS_TIME7);
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("ac_id",examId);
        if(competitionExamPaperRel.getPaperType()==4 || competitionExamPaperRel.getPaperType()==6){
            if(exam.getUnit()==0){
                //集体考试
                QueryWrapper<DrawResult>  q = new QueryWrapper<>();
                q.eq("ac_id",examId);
                q.eq("user_id",userId);
                DrawResult d = drawResultService.getOne(q);
                queryWrapper.eq("depart_id",d.getDepartId());
                ll = drawResultService.list(queryWrapper);
            }else{
                queryWrapper.eq("user_id",userId);
                DrawResult dr = drawResultService.getOne(queryWrapper);
                ll.add(dr);
            }
        }else if(competitionExamPaperRel.getPaperType()==5){
            ll = drawResultService.list(queryWrapper);
        }
        HashMap<Object,Object>  map = new HashMap<>();
        map.put("paperType",competitionExamPaperRel.getPaperType());
        map.put("drawList",ll);
        return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),ResultMesCode.SUCCESS.getResultMsg(),map);
    }

    @ApiOperation(value = "获取每个小组的分数")
    @ApiImplicitParam(name = "examId",value = "活动id",required = true,dataType = "Long")
    @RequestMapping(value = "/getTotalScoreByExamId",method = RequestMethod.GET)
    public ApiResult getTotalScoreByExamId(Long  examId){
        Exam exam = examService.getById(examId);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("ac_id",examId);
        List<DrawResult> list = drawResultService.list(queryWrapper);
        List<DrawResultVO>  ll = new ArrayList<>();
        if(exam.getUnit()==0){
            //团体竞答,每个单位做为一个分组
            Map<Long, List<DrawResult>> collect = list.stream().collect(Collectors.groupingBy(DrawResult::getDepartId));
            list.clear();
            for (Long departId:collect.keySet()) {
                list.add(collect.get(departId).get(0));
            }
        }
        list.sort((d1,d2)->Integer.valueOf(d1.getPlaceNum())-Integer.valueOf(d2.getPlaceNum()));
        for (DrawResult dr:list) {
            DrawResultVO vo = new DrawResultVO();
            BeanUtils.copyProperties(dr,vo);
            if(exam.getUnit()==0){
                //团体竞答
                String departName = sysDepartmentFeign.findSysDepartmentById(dr.getDepartId()).getDname();
                vo.setGroupName(departName);
            }else {
                String nickname = sysDepartmentFeign.findAppUserById(dr.getUserId()).getNickname();
                vo.setGroupName(nickname);
            }
            //vo.setGroupName("第"+ ChangeInt2chn.tranInt(Integer.valueOf(dr.getPlaceNum()))+"组");
//            /**
//              *
//              *  新增 录屏信息
//             **/
            QueryWrapper wrapper = new QueryWrapper();
            wrapper.eq("ac_id",vo.getAcId());
            wrapper.eq("paper_id",vo.getPaperId());
            wrapper.eq("user_id",vo.getUserId());
            List<Record> record_dbList = this.recordService.list(wrapper);
            if(CollectionUtils.isNotEmpty(record_dbList)){
                List<String> collect = record_dbList.stream().map(Record::getRecordPath).collect(Collectors.toList());
                vo.setRecordPathList(collect);
            }
            ll.add(vo);
        }
        return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),ResultMesCode.SUCCESS.getResultMsg(),ll);
    }


    /**
     * 选答试卷考试
     * @param paperId
     * @param examId
     * @return
     */
    @ApiOperation(value = "主持人点击下一组答题")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "examId",value = "活动id",required = true,dataType = "Long"),
            @ApiImplicitParam(name = "paperId",value = "试卷id",required = true,dataType = "Long"),
            @ApiImplicitParam(name = "palcenum",value = "座位号",required = true,dataType = "String")
    })
    @RequestMapping(value = "/changeAnswerGroup",method = RequestMethod.GET)
    public ApiResult changeAnswerGroup(Long examId,Long paperId,String palcenum){
        Paper paper = paperService.getById(paperId);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("paper_id",paperId);
        List<StudentAnswer> list2 = studentAnswerService.list(queryWrapper);
        queryWrapper.orderByAsc("sort");
        List<PaperManageRel> list = paperManageRelService.list(queryWrapper);
        List<QuestionVO> ll = new ArrayList<>();
        Exam exam = examService.getById(examId);
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        boolean ishost = false;
        if(exam.getHostId().equals(loginAppUser.getId())){
            ishost = true;
        }
        if(paper.getType()==6){
            //选答试卷 向考生返回所有的试题列表
            for (PaperManageRel pmr:list) {
                Question byId = questionService.getById(pmr.getQuestionId());
                QuestionVO  vo = new QuestionVO();
                BeanUtils.copyProperties(byId,vo);
                vo.setQuestionScore(pmr.getScore());
                vo.setQuestionSort(pmr.getSort());
                //根据不同用户选择是否返回试题答案和试题解析
                if(!ishost){
                    vo.setAnswer(null);
                    vo.setAnalysis(null);
                }
                if(paper.getType()==6){
                    //选答试卷,判断试题是否被占用
                    boolean b = list2.stream().anyMatch(stu -> stu.getQuestionId().equals(byId.getId()));
                    if (b){
                        vo.setCheck(true);
                    }
                }
                ll.add(vo);
            }
        }else if(paper.getType()==5){
            //抢答试卷，主持人选定答题组，其他小组需要展示获取抢答权的小组
            QueryWrapper queryWrapper1 = new QueryWrapper();
            queryWrapper1.eq("ac_id",examId);
            queryWrapper1.eq("paper_id",paperId);
            queryWrapper1.eq("place_num",palcenum);
            DrawResult one = drawResultService.getOne(queryWrapper1);
            return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),ResultMesCode.SUCCESS.getResultMsg(),one.getUserId());
        }

        return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),ResultMesCode.SUCCESS.getResultMsg(),ll);
    }

    /**
     *选答试卷  考生选择试题
     * 需要再主持人端 显示选择的试题
     */
    @ApiOperation(value = "考生选答 ，主持人或者考生选择试题")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "questionId",value = "试题id",dataType = "Long"),
            @ApiImplicitParam(name = "examId",value = "活动id",dataType = "Long"),
            @ApiImplicitParam(name = "host",value = "是否主持人",dataType = "Boolean"),
            @ApiImplicitParam(name = "paperId",value = "试卷id",dataType = "Long"),
            @ApiImplicitParam(name = "placeNum",value = "座位号",dataType = "String")
    })
    @RequestMapping(value = "/studentChooseQuestion",method = RequestMethod.GET)
    public ApiResult studentChooseQuestion(Long examId,Long paperId,Long questionId,boolean host,String placeNum){
        List<Long> list = new ArrayList<>();
        QueryWrapper<DrawResult> qw = new QueryWrapper<>();
        qw.eq("ac_id",examId);
        List<DrawResult> ls = drawResultService.list(qw);
        ls.stream().forEach(e->list.add(e.getUserId()));
        Exam exam = examService.getById(examId);
        list.add(exam.getHostId());
       /*
           if(host){
                QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("ac_id",examId);
                queryWrapper.eq("place_num",placeNum);
                List<DrawResult> ll = drawResultService.list(queryWrapper);
                ll.stream().forEach(e->list.add(e.getUserId()));
            }else{
                Exam exam = examService.getById(examId);
                list.add(exam.getHostId());
            }
        */
        QueryWrapper<PaperManageRel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("paper_id",paperId);
        queryWrapper.eq("question_id",questionId);
        PaperManageRel one = paperManageRelService.getOne(queryWrapper);
        Integer questionTime = one.getQuestionTime();
        Map<String,Object> map = new HashMap();

        List<Object> objects = redisUtils.lGet("competition:" + examId + ":" + paperId, 0, -1);
        List<QuestionVO> ll = (List<QuestionVO>)objects.get(0);
        int index = 0;
        for(int i =0 ;i<ll.size();i++){
            if(ll.get(i).getId().equals(questionId)){
                index  = i;
                break;
            }
        }
        map.put("userIds",list);
        map.put("index",index);
        map.put("time",questionTime);

        return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),ResultMesCode.SUCCESS.getResultMsg(),map);
    }

    /**
     *选答试卷  考生选择试题
     * 需要再主持人端 显示选择的试题
     */
    @ApiOperation(value = "主持人进行判题")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "paperId",value = "试卷id",dataType = "Long"),
            @ApiImplicitParam(name = "examId",value = "活动id",dataType = "Long"),
            @ApiImplicitParam(name = "qIndex",value = "试题下标",dataType = "Integer"),
            @ApiImplicitParam(name = "answer",value = "答案",dataType = "Object"),
            @ApiImplicitParam(name = "host",value = "是否主持人答题",dataType = "Boolean"),
            @ApiImplicitParam(name = "placeNum",value = "小组座位号",dataType = "String")
    })
    @RequestMapping(value = "/judgeStudentAnswer",method = RequestMethod.POST)
    public synchronized ApiResult judgeStudentAnswer(@RequestBody HashMap<String,Object> map){
        // 当前回答问题的学员id
        Long userId=null;
        Long questionId=null;
        String typeName=null;
        Long examId = Long.valueOf(String.valueOf(map.get("examId")));
        Long paperId = Long.valueOf(String.valueOf(map.get("paperId")));
        Integer qIndex = Integer.valueOf(String.valueOf(map.get("qIndex")));
        String placeNum = "";
        if(!Validator.isEmpty(map.get("placeNum"))){
            placeNum = String.valueOf(map.get("placeNum"));
        }
        Object answer  = new Object();
        if(!Validator.isEmpty(map.get("answer"))){
            answer = map.get("answer");
        }
        boolean host = false;
        if(!Validator.isEmpty(map.get("host"))){
            host = Boolean.valueOf(String.valueOf(map.get("host")));
        }

        List<Object> objects = redisUtils.lGet("competition:" + examId + ":" + paperId, 0, -1);
        List<QuestionVO> o = (List<QuestionVO>)objects.get(0);
        Question question = questionService.getById(o.get(qIndex).getId());
        questionId=question.getId();
        typeName=CommonPar.question_type.get(String.valueOf(question.getType())).toString();
        JSONObject  jsonObject = new JSONObject();
        //host==true  主持人协助考生进行答题，然后判题

        QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ac_id",examId);
        queryWrapper.eq("place_num",placeNum);
        List<DrawResult> list = drawResultService.list(queryWrapper);
        userId=list.get(0).getUserId();

        if(host){
            QueryWrapper<StudentAnswer> queryWrapper1  = new QueryWrapper<>();
            queryWrapper1.eq("paper_id",paperId);
            queryWrapper1.eq("question_id",question.getId());
            queryWrapper1.eq("student_id",list.get(0).getUserId());
            List<StudentAnswer> list1 = studentAnswerService.list(queryWrapper1);

            Exam exam = examService.getById(examId);
            Integer t = exam.getScoringMethod();
            QueryWrapper<PaperManageRel> qw = new QueryWrapper<>();
            qw.eq("paper_id",paperId);
            qw.eq("question_id",question.getId());

            PaperManageRel one = paperManageRelService.getOne(qw);
            double s = one.getScore();

            StudentAnswer studentAnswer = new StudentAnswer();
            Paper paper = paperService.getById(paperId);
            JSONObject  js = new JSONObject();
            if(question.getType()==2){

                if(Validator.isEmpty(answer)){
                    List<String> ll = new ArrayList<>();
                    js.put("text",ll);
                }else{
                    List<String> ll = (List<String>)answer;
                    JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(ll));
                    js.put("text",jsonArray);
                }
            }else{
                if(Validator.isEmpty(answer)){
                    js.put("text","");
                }else{
                    js.put("text",answer);
                }
            }
            //String text = js.getString("text");
            boolean b = judgeAnswer(js, question);

            double score = 0L;
            if(b){
                jsonObject.put("isCorrect",true);
                studentAnswer.setActualScore(s);
                for (DrawResult dr : list ) {
                    score = dr.getScore()+s;
                    dr.setScore(dr.getScore()+s);
                    if(list1.size()<1){
                        drawResultService.saveOrUpdate(dr);
                    }

                }
                if(list1.size()<1){
                    Tools.putCompetitionScoreCache(examId+"&"+placeNum,score);
                }


            }else {
                jsonObject.put("isCorrect",false);
                if(t==0){
                    for (DrawResult dr : list ) {
                        score = dr.getScore()-s<0?0:dr.getScore()-s;
                        dr.setScore(score);
                        if(list1.size()<1){
                            drawResultService.saveOrUpdate(dr);
                        }
                    }
                    if(list1.size()<1){
                        Tools.putCompetitionScoreCache(examId+"&"+placeNum,score);
                    }

                }
            }


            studentAnswer.setQuestionId(o.get(qIndex).getId());
            studentAnswer.setStuAnswer(js.toJSONString());
            studentAnswer.setPaperId(paperId);
            studentAnswer.setPaperType(paper.getType());
            studentAnswer.setCreateTime(new Date());
            studentAnswer.setType(question.getType());
            studentAnswer.setStudentId(list.get(0).getUserId());
            studentAnswer.setPlaceNum(placeNum);
            studentAnswer.setAcId(examId);
            if(list1.size()<0){
                studentAnswerService.save(studentAnswer);
            }

        }else {
            QueryWrapper<StudentAnswer> stuqueryWrapper = new QueryWrapper();
            stuqueryWrapper.eq("question_id",o.get(qIndex).getId());
            stuqueryWrapper.eq("paper_id",paperId);
            List<StudentAnswer> studentAnswerList = studentAnswerService.list(stuqueryWrapper);
            if(studentAnswerList==null || studentAnswerList.size()<1){
                jsonObject.put("isCorrect",false);
            }else {
                StudentAnswer studentAnswer = studentAnswerList.get(0);
                if(studentAnswer.getActualScore()!=null  && studentAnswer.getActualScore()>0){
                    jsonObject.put("isCorrect",true);
                }else {
                    jsonObject.put("isCorrect",false);
                }
            }
        }
        JSONObject jsonObject1 = JSONObject.parseObject(question.getAnswer());
        String answers = jsonObject1.getString("text");
        if(question.getType()==2){
            String[] split = answers.split(",");
            jsonObject.put("questionAnswer",split);
        }else {
            jsonObject.put("questionAnswer",answers);
        }
        // 增加错题收录
        if (Boolean.valueOf(jsonObject.get("isCorrect").toString())==false){
            Map<String, Object> parMap = new HashMap<>();
            // 根据用户id和试题id查询是否已记录
            parMap.put("userId", userId);
            parMap.put("questionId", questionId);
            List<Map<String, Object>> byPar = errorQuestionDao.findByPar(parMap);
//            byPar = CollectionsCustomer.builder().build().listMapToLowerCase(byPar);
            if (byPar == null || byPar.size() == 0) {
                parMap.put("question", question.getQuestion());
                parMap.put("type", typeName);
                parMap.put("createTime", new Timestamp(System.currentTimeMillis()));
                errorQuestionDao.saveInfo(parMap);
            } else {
                parMap.put("id", Long.valueOf(byPar.get(0).get("id").toString()));
                parMap.put("createTime", new Timestamp(System.currentTimeMillis()));
                errorQuestionDao.updateInfo(parMap);
            }
        }
        Tools.putJudgeCache(paperId+":"+o.get(qIndex).getId(),true);
        return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),ResultMesCode.SUCCESS.getResultMsg(),jsonObject.toJSONString());
    }

    @ApiOperation(value = "加时试卷主持人选择指定的组进行答题")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "paperId",value = "试卷id",dataType = "Long"),
            @ApiImplicitParam(name = "examId",value = "活动id",dataType = "Long"),
    })
    @RequestMapping(value = "/addoverTimeGroupsByhost",method = RequestMethod.POST)
    public  ApiResult   addoverTimeGroupsByhost(@RequestBody HashMap<String,Object> map){
        Long paperId  = Long.valueOf(String.valueOf(map.get("paperId")));
        Long examId  = Long.valueOf(String.valueOf(map.get("examId")));
        List<String>  placeNums = (List<String>)map.get("placeNums");
        Paper paper = paperService.getById(paperId);
        //Exam  exam = examService.getById(examId);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("ac_id",examId);
        queryWrapper.eq("paper_id",paperId);
        List<QuestionVO>  ll = getAllQuestionByPaperId(paperId);
        CompetitionExamPaperRel competitionExamPaperRel = competitionExamPaperRelService.getOne(queryWrapper);
        if(competitionExamPaperRel.getIsOvertime()==1){
            if(paper.getType().equals(ExamConstants.PAPER_BIDA)){
                //必答试卷,提前组装试题对应关系
                List<DrawResult> list = new ArrayList<>();
                for (String str:placeNums) {
                    QueryWrapper<DrawResult>  qw = new QueryWrapper<>();
                    qw.eq("ac_id",examId);
                    qw.eq("place_num",str);
                    DrawResult one = drawResultService.list(qw).get(0);
                    list.add(one);
                }
                //试题id-->对应答题人员id
                HashMap<String, Object> longListHashMap = SpiltQuestionUtils.spiltQuestion(list, ll);
                //轮答方式，保存必答试卷的话把每个小组对应的必答试题对应信息保存到redis
                redisUtils.delKeys("competition:question:"+examId+":"+paperId);
                redisUtils.hmset("competition:question:"+examId+":"+paperId,longListHashMap,ExamConstants.EXAM_OTHER_DETAILS_TIME7);
            }
        }

        return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),ResultMesCode.SUCCESS.getResultMsg(),null);
    }

    public  List<QuestionVO>  getAllQuestionByPaperId(Long paperId){
        QueryWrapper queryWrapper1 = new QueryWrapper();
        queryWrapper1.eq("paper_id",paperId);
        List<StudentAnswer> list2 = studentAnswerService.list(queryWrapper1);
        Paper paper = paperService.getById(paperId);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("paper_id",paperId);
        queryWrapper.orderByAsc("sort");
        List<PaperManageRel> list = paperManageRelService.list(queryWrapper);
        List<QuestionVO> ll = new ArrayList<>();
        for (PaperManageRel pmr:list) {
            Question byId = questionService.getById(pmr.getQuestionId());
            QuestionVO  vo = new QuestionVO();
            Object judgeCache = Tools.getJudgeCache(paperId + ":" + byId.getId());
            if(!Validator.isEmpty(judgeCache)){
                Boolean aBoolean = Boolean.valueOf(judgeCache.toString());
                if(aBoolean){
                    vo.setCheck(true);
                }
            }
            BeanUtils.copyProperties(byId,vo);
            vo.setQuestionScore(pmr.getScore());
            vo.setQuestionSort(pmr.getSort());
            vo.setRelId(pmr.getId());
            vo.setPaperType(paper.getType());
            vo.setType(byId.getType());
            vo.setQuestionTime(pmr.getQuestionTime());
            if(paper.getType()==6){
                //选答试卷,判断试题是否被占用
                boolean b = list2.stream().anyMatch(stu -> stu.getQuestionId().equals(byId.getId()));
                if (b){
                    vo.setCheck(true);
                    //vo.setCheck(false);
                }
            }
            ll.add(vo);
        }
       /* Collections.sort(ll, new Comparator<QuestionVO>() {
            @Override
            public int compare(QuestionVO o1, QuestionVO o2) {
                return o1.getType() - o2.getType();
            }
        });
        Map<Integer, List<QuestionVO>> collect = ll.stream().collect(Collectors.groupingBy(QuestionVO::getType));
        for (Integer i:collect.keySet()) {
            List<QuestionVO> list1 = collect.get(i);
            Collections.sort(list1, new Comparator<QuestionVO>() {
                @Override
                public int compare(QuestionVO o1, QuestionVO o2) {
                    return o1.getQuestionSort()-o2.getQuestionSort();
               }
            });
        }*/
        Tools.putJudgeCache(paperId,ll);
        return ll;
    }

    public boolean judgeAnswer(JSONObject jsonObject,Question byId){
        boolean isCorrect = false;
        if(byId.getType()==1 || byId.getType()==3){
            JSONObject jsonObject1 = JSONObject.parseObject(byId.getAnswer());
            if(jsonObject.getString("text").equals(jsonObject1.getString("text"))){
                isCorrect = true;
            }
        }else if(byId.getType()==2){
                JSONArray o = JSONArray.parseArray(jsonObject.getString("text"));
                if(!Validator.isEmpty(o)&&o.size()>0){
                    List ls = new ArrayList();
                    String value  = "";
                    for(int i=0;i<o.size();i++){
                        ls.add(o.get(i));
                    }
                    Collections.sort(ls);
                    for(int i=0;i<ls.size();i++){
                        if(i>0){
                            value+=",";
                        }
                        value+=ls.get(i);
                    }
                    JSONObject jos = JSONObject.parseObject(byId.getAnswer());
                    String text = (String)jos.get("text");
                    String[] split = text.split(",");
                    List ll = new ArrayList();
                    for(int i = 0;i<split.length;i++){
                        ll.add(split[i]);
                    }
                    Collections.sort(ll);
                    String v  = "";
                    for(int i=0;i<ll.size();i++){
                        if(i>0){
                            v+=",";
                        }
                        v+=ll.get(i);
                    }
                    if(value.equals(v)){
                        isCorrect = true;
                    }
                }


        }
        return isCorrect;
    }

    @ApiOperation(value = "主持人修改小组分数")
    @RequestMapping(value = "/editGroupScore",method = RequestMethod.POST)
    public ApiResult  editGroupScore(@RequestBody HashMap<String,Object> map){
        Object examId = map.get("examId");
        Object placeNum = map.get("placeNum");
        Object number = map.get("number");
        QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ac_id",Long.valueOf(String.valueOf(examId)));
        queryWrapper.eq("place_num",placeNum.toString());
        List<DrawResult> list = drawResultService.list(queryWrapper);

        list.stream().forEach(e->{
            e.setScore(Double.parseDouble(number.toString()));
        });
        drawResultService.saveOrUpdateBatch(list);
        return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),ResultMesCode.SUCCESS.getResultMsg(),Double.parseDouble(number.toString()));
    }

    @ApiOperation(value = "考生选择试题难度后给主持人发送消息")
    @ApiImplicitParams({@ApiImplicitParam(name = "examId",value = "活动id", required = true,dataType = "Long"),
            @ApiImplicitParam(name = "difficuty",value = "难度",required = true,dataType = "String"),
            @ApiImplicitParam(name = "userId",value = "当前答题人id", required = true,dataType = "Long")})
    @RequestMapping(value = "chooseQuestionDifficty",method = RequestMethod.GET)
    public ApiResult chooseQuestionDifficty(Long examId,String difficuty,Long userId){
        Long hostId = examService.getById(examId).getHostId();
        return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),ResultMesCode.SUCCESS.getResultMsg(),hostId);
    }

    @ApiOperation(value = "考生选择答案后给主持人发送消息")
    @RequestMapping(value = "/sendStuAnswers2host",method = RequestMethod.POST)
    public ApiResult sendStuAnswers2host(@RequestBody Map<Object,Object> map){
        Object examId = map.get("examId");
        return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),ResultMesCode.SUCCESS.getResultMsg(),examId);
    }

    @ApiOperation(value = "主持人点击结束活动")
    @RequestMapping(value = "finishExamByhost",method = RequestMethod.GET)
    public ApiResult finishExamByhost(Long examId){
        Exam exam = examService.getById(examId);
        exam.setExamStatus(ExamConstants.EXAM_FINISH);
        examService.saveOrUpdate(exam);
        examKpPersonAvgScoreService.calculate(examId);
        QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ac_id",examId);
        List<DrawResult> list = drawResultService.list(queryWrapper);
        list.stream().forEach(e->e.setUserStatus(ExamConstants.ACTIVITY_FINISH));
        drawResultService.saveOrUpdateBatch(list);
        return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),ResultMesCode.SUCCESS.getResultMsg(),examId);
    }
}

