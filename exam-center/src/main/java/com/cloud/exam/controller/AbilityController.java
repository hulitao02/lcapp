package com.cloud.exam.controller;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.core.ExamConstants;
import com.cloud.exam.model.exam.DrawResult;
import com.cloud.exam.model.exam.Exam;
import com.cloud.exam.model.record.Record;
import com.cloud.exam.service.*;
import com.cloud.exam.vo.DrawResultVO;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.user.AppUser;
import com.cloud.model.user.SysDepartment;
import com.cloud.utils.ExcelFileUtils;
import com.cloud.utils.RedisUtils;
import com.cloud.utils.Validator;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dyl on 2021-6-7.
 */
@RestController
public class AbilityController {

    public final static Logger logger = LoggerFactory.getLogger(AbilityController.class);
    @Autowired
    private DrawResultService drawResultService;
    @Autowired
    private ExamService examService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private SysDepartmentFeign sysDepartmentFeign;
    @Autowired
    private PaperService paperService;
    @Autowired
    private EvalService evalService;
    @Autowired
    EvalNewService evalNewService;

    @Autowired
    private RecordService recordService;


    @ApiOperation(value = "获取考生考试成绩")
    @PostMapping("/getStudentScoreByExamId")
    public ApiResult getStudentScoreByExamId(@RequestBody  Map<String,String> map){
        String examId = map.get("examId");
        String paperTypes = map.get("paperType");
        String departIds = map.get("departId");
        boolean flag = false;
        QueryWrapper<DrawResult> qw = new QueryWrapper<>();
        qw.eq("ac_id",Long.valueOf(examId));
        if(Validator.isNotNull(paperTypes)){
            List ll = new ArrayList();
            String[] split = paperTypes.split(",");
            for(int i = 0;i<split.length;i++){
                ll.add(Long.valueOf(split[i]));
            }
            qw.in("paper_type",ll);
        }
        if(Validator.isNotNull(departIds)){
            List ll = new ArrayList();
            String[] split = departIds.split(",");
            for(int i = 0;i<split.length;i++){
                ll.add(Long.valueOf(split[i]));
            }
            qw.in("depart_id",ll);
        }
        //qw.orderByDesc("score");
        List<DrawResult> li = new ArrayList<>();
        List<DrawResult> list = drawResultService.list(qw);
        list.stream().forEach(ee->li.add(ee));
        QueryWrapper<Exam> e = new QueryWrapper<>();
        e.eq("parent_id",Long.valueOf(examId));
        List<Exam> list1 = examService.list(e);
        if (list1.size()>0){
            flag = true;
        }
        for(Exam exam:list1){
            double scorePercent = exam.getScorePercent();
            QueryWrapper<DrawResult> qw1 = new QueryWrapper<>();
            qw1.eq("ac_id",exam.getId());
            List<DrawResult> list2 = drawResultService.list(qw1);
            list2.stream().forEach(ew->{
                BigDecimal b1 = new BigDecimal(ew.getScore());
                BigDecimal b2 = new BigDecimal(scorePercent);
                Long round = Math.round(b1.multiply(b2).doubleValue());
                ew.setScore(round.doubleValue());
                li.add(ew);
            });
        }

        List<DrawResultVO>  ll = new ArrayList<>();
        for (DrawResult dr:li) {
            DrawResultVO vo = new DrawResultVO();
            BeanUtils.copyProperties(dr,vo);
            vo.setScore(vo.getScore()==null?0:vo.getScore());
            AppUser  appUser  = sysDepartmentFeign.findAppUserById(dr.getUserId());
            vo.setUserName(appUser.getNickname());
            vo.setDepartName(appUser.getDepartmentName());
            vo.setPaperName(paperService.getById(dr.getPaperId()).getPaperName());
            /**
             *  新增 录屏信息
             **/
            QueryWrapper wrapper = new QueryWrapper();
            wrapper.eq("ac_id",vo.getAcId());
            wrapper.eq("paper_id",vo.getPaperId());
            wrapper.eq("user_id",vo.getUserId());
            List<Record> record_dbList = this.recordService.list(wrapper);
            if(CollectionUtils.isNotEmpty(record_dbList) && record_dbList.size()>0){
                List<String> collect = record_dbList.stream().map(Record::getRecordPath).collect(Collectors.toList());
                vo.setRecordPathList(collect);
            }
            ll.add(vo);
        }
        List<DrawResultVO> collect = ll.stream().sorted(Comparator.comparing(DrawResultVO::getScore).reversed()).collect(Collectors.toList());

        redisUtils.set("exam:"+"excel:"+Long.valueOf(examId),collect, ExamConstants.EXAM_OTHER_DETAILS_TIME7);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "", collect);
    }

    /**
     * 获取当前活动下参加考试人员所属单位
     * @param examId
     * @return
     */
    @RequestMapping("/getDepartmentRelByExamId")
    public ApiResult getDepartmentRelByExamId(Long examId){
        List ll = new ArrayList();
        QueryWrapper<DrawResult> qw = new QueryWrapper<>();
        qw.eq("ac_id",Long.valueOf(examId));
        List<DrawResult> list = drawResultService.list(qw);
        Map<Long, List<DrawResult>> collect = list.stream().collect(Collectors.groupingBy(DrawResult::getDepartId));
        Set<Long> departIds = collect.keySet();
        for (Long id:departIds) {
            SysDepartment sysDepartmentById = sysDepartmentFeign.findSysDepartmentById(id);
            ll.add(sysDepartmentById);
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "", ll);
    }

    /**
     * 导出考生成绩
     * @param examId
     * @param rs
     */
    @RequestMapping("/exportScoreExcel")
    public void  exportScoreExcel(Long examId,HttpServletResponse rs){
        Object object = redisUtils.get("exam:" + "excel:" + examId);
        List<DrawResultVO>  list = (ArrayList<DrawResultVO>)object;
        List<Map<String,Object>> set = new ArrayList<>();

        int j = 0;
        for (DrawResultVO vo:list) {
            j++;
            Map<String,Object> drawResultMap = new LinkedHashMap<>();
            drawResultMap.put("排名",j);
            drawResultMap.put("学员",vo.getUserName());
            drawResultMap.put("考核单位",vo.getDepartName());
            drawResultMap.put("考核试卷",vo.getPaperName());
            if(vo.getPaperType()== 0){
                drawResultMap.put("试卷类型","理论考试");
            }else if(vo.getPaperType()== 1){
                drawResultMap.put("试卷类型","情析考试");
            }else if(vo.getPaperType()== 2){
                drawResultMap.put("试卷类型","实操考试");
            }
            drawResultMap.put("分数",vo.getScore().intValue());
            set.add(drawResultMap);
        }
        String[] columns = {"排名","学员", "考核单位", "考核试卷","试卷类型", "分数"};
        String[] fields = { "","userName","departName","paperName", "paperType", "score"};
        Map map = MapUtil.newHashMap(true);
        for (int i = 0;i<fields.length;i++) {
            map.put(fields[i],columns[i]);
        }
        try {
            ExcelFileUtils.exportFile(set,map, "学员成绩排名表", rs);
        } catch (Exception e) {
            logger.error("导出文件失败...", e.getMessage());
        }
    }

    @ApiOperation(value = "跳转到能力评价页面")
    @RequestMapping("/toAbilityPageByexamId")
    public ApiResult toAbilityPageByexamId(Long examId)throws Exception{
//        evalService.makeEvaluation(examId);
        evalNewService.makeEvaluation(examId);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "", null);
    }
}
