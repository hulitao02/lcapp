package com.cloud.model.controller.personalconter;

import com.alibaba.fastjson.JSON;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.model.bean.vo.StudyPlanVo;
import com.cloud.model.service.CollectKnowledgeService;
import com.cloud.model.service.KnowledgeViewService;
import com.cloud.model.service.StudyPlanService;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@ApiModel("统计接口")
@RestController
@RequestMapping("/personStatistics")
public class PersonalStatisticsController {

    @Autowired
    private CollectKnowledgeService collectKnowledgeService;

    @Autowired
    private StudyPlanService studyPlanService;

    @Autowired
    private KnowledgeViewService knowledgeViewService ;


    /**
     * 查询某个年份下，用户每个月学习的统计
     *
     * @param paramsDate
     * @param userId
     * @return
     */
    @ApiOperation("按照年份查询用户每个月学习的总量")
    @RequestMapping(value = "/statisticsStudyCount", method = RequestMethod.GET)
    private ApiResult statisticsStudyCount(@RequestParam("paramsDate") String paramsDate,
                                           @RequestParam("userId") Integer userId) {

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", userId);
        paramMap.put("paramsDate", paramsDate);
        List<Map<String, Object>> resultListMap = this.knowledgeViewService.statisticsStudyCount(paramMap);
//      最后返回结果
        List<Map<String, Object>> sortedListMap = new ArrayList<>();
//      KEY值
        String time_pre = paramsDate;
//      TODO 补全每个月的学习统计,排序
        for (int i = 1; i <= 12; i++) {
            String pre_date = paramsDate + "-" + (i < 10 ? "0" + i : i);
            Optional<Map<String, Object>> selectMap = resultListMap.stream().filter(eleMap -> {
                if (eleMap.containsValue(pre_date)) {
                    return true;
                }
                ;
                return false;
            }).findFirst();
            if (!selectMap.isPresent()) {
                Map<String, Object> mapNull = new HashMap<>();
                mapNull.put("date", pre_date);
                mapNull.put("count", 0);
                sortedListMap.add(mapNull);
            } else {
                Map<String, Object> stringObjectMap = selectMap.get();
                sortedListMap.add(stringObjectMap);
            }
        }
        return ApiResultHandler.buildApiResult(200, "学习统计总量", sortedListMap);
    }


    /**
     * 查询某个年份下，每个学习计划结束点完成的时间
     *
     * @param paramsDate
     * @param userId
     * @return
     */
    @ApiOperation("查询某个年份下，统计学习计划每个月完成情况")
    @RequestMapping(value = "/statisticsStudyPlan", method = RequestMethod.GET)
    private ApiResult statisticsStudyPlan(@RequestParam("paramsDate") String paramsDate,
                                          @RequestParam("userId") Integer userId) {

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("paramDate", paramsDate);
        paramMap.put("status", 2);
//      查询出当前 年份所有的学习计划情况
        List<Map<String, Object>> fineshedMapList = this.studyPlanService.getStatisticsStudyPlansWithStudyStatus(paramMap);
        paramMap.remove("status");
        List<Map<String, Object>> unFinishedList = this.studyPlanService.getStatisticsStudyPlansWithStudyStatus(paramMap);
        /**
         *  最终返回结果
         */
        List<Map<String, Object>> resultList = new ArrayList<>();
        /**
         *  补全 12个月每个月的完成
         */
        for (int i = 1; i <= 12; i++) {
            String pre_date = paramsDate + "-" + (i < 10 ? "0" + i : i);
            Map<String,Object> newEleMap = new HashMap<>();
            Optional<Map<String, Object>> returnFinishedMap = fineshedMapList.stream().filter(map -> {
                return map.values().contains(pre_date);
            }).findFirst();
            if (returnFinishedMap.isPresent()) {
                Map<String, Object> stringObjectMap = returnFinishedMap.get();
                newEleMap.put("date",pre_date);
                newEleMap.put("done",stringObjectMap.get("count"));
            } else {
                newEleMap.put("date",pre_date);
                newEleMap.put("done",0);
            }
//          未完成的计划统计
            Optional<Map<String, Object>> returnUnMap = unFinishedList.stream().filter(map -> {
                return map.values().contains(pre_date);
            }).findFirst();
            if (returnUnMap.isPresent()) {
                Map<String, Object> stringObjectMap = returnUnMap.get();
                newEleMap.put("date",pre_date);
                newEleMap.put("undone",stringObjectMap.get("count"));
            } else {
                newEleMap.put("date",pre_date);
                newEleMap.put("undone",0);
            }
            resultList.add(newEleMap);
        }
        return ApiResultHandler.buildApiResult(200, "学习计划，统计每个月完成情况", resultList);
    }


    public static void main(String[] args) throws ParseException {

        List<StudyPlanVo> studyPlanVos = new ArrayList<>();
        StudyPlanVo studyPlanVo = new StudyPlanVo();
        studyPlanVo.setId(1);
        String time1 = "2021-10-31";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        studyPlanVo.setPlanStartTime(simpleDateFormat.parse(time1));
        studyPlanVo.setStudyedCount(1);
        studyPlanVo.setUnstudyedCount(1);
        studyPlanVos.add(studyPlanVo);

        StudyPlanVo studyPlanVo3 = new StudyPlanVo();
        studyPlanVo3.setId(1);
        String time3 = "2021-10-31";
        studyPlanVo3.setPlanStartTime(simpleDateFormat.parse(time3));
        studyPlanVo3.setStudyedCount(1);
        studyPlanVo3.setUnstudyedCount(1);
        studyPlanVos.add(studyPlanVo3);


        StudyPlanVo studyPlanVo2 = new StudyPlanVo();
        studyPlanVo2.setId(2);
        String time2 = "2021-11-31";
        studyPlanVo2.setPlanStartTime(simpleDateFormat.parse(time2));
        studyPlanVo2.setStudyedCount(2);
        studyPlanVo2.setUnstudyedCount(2);
        studyPlanVos.add(studyPlanVo2);

//      分组后计算SUM总量
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM");
        Map<String, List<StudyPlanVo>> collect = studyPlanVos.stream().collect(Collectors.groupingBy(vo -> {
            return simpleDateFormat2.format(vo.getPlanStartTime());
        }));

        Map<String, Map<Integer, Long>> resultMap = new HashMap<>();
        for (Map.Entry<String, List<StudyPlanVo>> entry : collect.entrySet()) {
            String timeKey = entry.getKey();
            List<StudyPlanVo> sameKeyList = entry.getValue();
            IntSummaryStatistics collectStudyed = sameKeyList.stream().collect(Collectors.summarizingInt(vo -> vo.getStudyedCount()));
            long studySum = collectStudyed.getSum();
            IntSummaryStatistics uncollect = sameKeyList.stream().collect(Collectors.summarizingInt(vo -> vo.getUnstudyedCount()));
            long unSum = uncollect.getSum();
            Map<Integer, Long> map = new HashMap<>();
            map.put(1, studySum);
            map.put(0, unSum);
            resultMap.put(timeKey, map);
        }


        System.out.println(JSON.toJSONString(resultMap));


    }


}
