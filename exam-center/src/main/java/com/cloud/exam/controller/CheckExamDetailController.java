package com.cloud.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.exam.model.exam.*;
import com.cloud.exam.service.*;
import com.cloud.exam.vo.CheckExamVO;
import com.cloud.exam.vo.ConditionBeanVO;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.model.common.KnowledgePoints;
import com.cloud.utils.RedisUtils;
import com.cloud.utils.Validator;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by dyl on 2021/08/17.
 */
@Api("活动信息查询处理类")
@RestController
@RequestMapping("/checkExam")
public class CheckExamDetailController {

    @Autowired
    private ExamService examService;
    @Autowired
    private PaperService  paperService;
    @Autowired
    private ManageBackendFeign manageBackendFeign;
    @Autowired
    private DrawResultService drawResultService;
    @Autowired
    private PaperManageRelService paperManageRelService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private RedisUtils redisUtils;


    @RequestMapping(value = "/getExamDetailsByContition",method = RequestMethod.POST)
    public ApiResult getExamDetailsByContition(@RequestBody ConditionBeanVO conditionBeanVO){

        Long userId = conditionBeanVO.getUserId();
        Integer page = conditionBeanVO.getPage();
        Integer size = conditionBeanVO.getSize();
        Page pg = new Page(page, size);
        List<Integer> types = conditionBeanVO.getTypes();
        List<Integer> examStatus = conditionBeanVO.getExamStatus();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id",userId);
        if(!Validator.isEmpty(types) && types.size()>0){
            queryWrapper.in("paper_type",types);
        }
        if(!Validator.isEmpty(examStatus) && examStatus.size()>0){
            List<Integer>  ll =  new ArrayList<>();
            if(examStatus.contains(1)){
                ll.add(0);
            }
            ll.addAll(examStatus);
            queryWrapper.in("user_status",ll);
        }
        queryWrapper.isNotNull("paper_type");
        IPage<DrawResult> page1 = drawResultService.page(pg, queryWrapper);

        page1.convert(dr->{
            CheckExamVO v = new CheckExamVO();
            Exam  exam  = examService.getById(dr.getAcId());
            Paper paper = paperService.getById(dr.getPaperId());
            v.setExamDate(exam.getStartTime());
            v.setExamId(dr.getAcId());
            v.setExamName(exam.getName());
            v.setExamTime(paper.getTotalTime());
            List<KnowledgePoints> knowledgePointsBypaperId = getKnowledgePointsBypaperId(dr.getPaperId());
            v.setKList(knowledgePointsBypaperId);
            return v;
        });

        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", page1);
    }

    public List<KnowledgePoints>  getKnowledgePointsBypaperId(Long  paperId){
        List<KnowledgePoints> ll =  new ArrayList<>();
        QueryWrapper<PaperManageRel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("paper_id",paperId);
        List<PaperManageRel> list = paperManageRelService.list(queryWrapper);
        list.stream().forEach(e->{
            Long questionId = e.getQuestionId();
            Question byId = questionService.getById(questionId);
            String kpId = byId.getKpId();
            KnowledgePoints knowledgePointsById = manageBackendFeign.getKnowledgePointsById(kpId);
            ll.add(knowledgePointsById);
        });
        ArrayList<KnowledgePoints> collect = ll.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(t -> t.getId()))), ArrayList::new));
        return collect;
    }
}

