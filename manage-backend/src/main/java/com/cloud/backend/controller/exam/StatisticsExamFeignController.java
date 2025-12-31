package com.cloud.backend.controller.exam;


import com.cloud.feign.exam.ExamFeign;
import com.cloud.model.exam.ExamStatisticsDto;
import com.cloud.model.user.AppUser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    考试统计 controller
 */
@RestController
@ApiModel("考试，学习统计")
@Slf4j
public class StatisticsExamFeignController {

    @Autowired
    private ExamFeign examFeign;


    @GetMapping(value = "/statistics/groupbyType")
    public List<Map> statisticsByQuestionType() {
        log.info("查询考试统计，通过试题类型");
        List<Map> questionStatisticsByType = this.examFeign.getQuestionStatisticsByType();
        return questionStatisticsByType;
    }

    @GetMapping(value = "/statistics/groupbyTypeAndDifficulty")
    public List<Map> getQuestionStatisticsByTypeAndDifficulty() {
        log.info("查询考试统计，通过试题类型和难度");
        List<Map> staticsResult = this.examFeign.getQuestionStatisticsByTypeAndDifficulty();
        return staticsResult;
    }

    @GetMapping(value = "/statistics/questionsIsUsed")
    public List<Map> getQuestionStatisticsByIsUsed() {
        log.info("查询考试统计，试题是否被使用");
        List<Map> staticsResult = this.examFeign.getQuestionStatisticsByIsUsed();
        if(CollectionUtils.isNotEmpty(staticsResult)){
            if(staticsResult.size() == 1){
                Map map = staticsResult.get(0);
                if(map.containsValue(1)) {
                    Map map1 = new HashMap();
                    map1.put("status",0);
                    map1.put("count",0);
                    staticsResult.add(map1);
                }else{
                    Map map1 = new HashMap();
                    map1.put("status",1);
                    map1.put("count",0);
                    staticsResult.add(map1);
                }
            }
        }else{
            Map map1 = new HashMap();
            map1.put("status",0);
            map1.put("count",0);
            staticsResult.add(map1);

            Map map2 = new HashMap();
            map2.put("status",1);
            map2.put("count",0);
            staticsResult.add(map2);
        }
        return staticsResult;
    }



    /**
     *  以年为单位，统计每个月开展训练任务数量、参加训练人数。【名称：各月训练人员数量和参训人数统计
     * @param examStatisticsDto
     * @return
     */
    @ApiOperation(value="以年为单位，统计每个月开展训练任务数量、参加训练人数。[名称：各月训练人员数量和参训人数统计]")
    @RequestMapping(value = "/statistics/getStatisticsExamCountAndUserCount", method = RequestMethod.POST)
    public List<Map> getStatisticsExamCountAndUserCount(@RequestBody ExamStatisticsDto examStatisticsDto){
        return this.examFeign.getStatisticsExamCountAndUserCount(examStatisticsDto);
    };

    /**
     *  以年为单位，排名自测数量最多的前10个学员，展示姓名、自测次数和自测题数
     * @param examStatisticsDto
     * @return
     */
    @ApiOperation(value="以年为单位，排名自测数量最多的前10个学员，展示姓名、自测次数和自测题数 [名称：自测前10名学员]")
    @RequestMapping(value = "/statistics/getStatisticsUsersExamAndQuestionCount", method = RequestMethod.POST)
    public Map getStatisticsUsersExamAndQuestionCount(@RequestBody ExamStatisticsDto examStatisticsDto){
        return this.examFeign.getStatisticsUsersExamAndQuestionCount(examStatisticsDto);
    };
    /**
     * 正在自测的人员
     * examStatisticsDto
     * @param
     * @return
     */
    @ApiOperation(value="列表列出，当前有哪些人正在自测 [名称：正在训练的学员]")
    @RequestMapping(value = "/statistics/getStatisticsExamingUserList", method = RequestMethod.POST)
    public List<Map> getStatisticsExamingUserList(@RequestBody ExamStatisticsDto examStatisticsDto) {
        return this.examFeign.getStatisticsExamingUserList(examStatisticsDto);
    };

    /**
     * 正在进行中的 训练
     *examStatisticsDto
     * @param
     * @return
     */
    @ApiOperation(value="当前有哪些训练任务有人在训练（只有有人进入训练还未结束就算），正在训练人数 [名称：正在进行的[考核|自测] ]")
    @RequestMapping(value = "/statistics/getStatisticsExamListByTypeAndStatus", method = RequestMethod.POST)
    public List<Map> getStatisticsExamListByTypeAndStatus(@RequestBody ExamStatisticsDto examStatisticsDto){
        return this.examFeign.getStatisticsExamListByTypeAndStatus(examStatisticsDto);
    };

    /**
     * 正在进行中的 训练
     * examStatisticsDto
     * @param
     * @return
     */
    @ApiOperation(value = "通过考试ID查询 正在参数活动人员信息")
    @RequestMapping(value = "/statistics/getUserListByExamId", method = RequestMethod.POST)
    public List<AppUser> getExamingUserList(@RequestBody ExamStatisticsDto examStatisticsDto){
        return this.examFeign.getExamingUserList(examStatisticsDto);
    };


    @ApiOperation(value = "以年为单位，统计每个月在线考核试卷创建数量和使用数量 [名称：年度试卷使用情况]")
    @RequestMapping(value = "/statistics/getStatisticsPaperCount", method = RequestMethod.POST)
    public List<Map> getStatisticsPaperCount(@RequestBody ExamStatisticsDto examDTO) {
        return this.examFeign.getStatisticsPaperCount(examDTO);
    };

    /**
     * 以年为单位，统计每个月在线考核活动数量、使用试卷数量（被活动关联的）、参考人数")
     * @param examDTO
     * @return
     */
    @ApiOperation(value = "以年为单位，统计每个月在线考核活动数量、使用试卷数量（被活动关联的）、参考人数 [名称：年度考核情况]")
    @RequestMapping(value = "/statistics/getStatisticsExPUCount", method = RequestMethod.POST)
    public Map<String,List<String>> getStatisticsExPUCount(@RequestBody ExamStatisticsDto examDTO){
        return this.examFeign.getStatisticsExPUCount(examDTO);
    } ;

    /**
     * 以年为单位，列出所有竞答活动，每次的参与人数、使用试题数量和活动时间（开始时间）
     *
     * @param examDTO
     * @return
     */
    @ApiOperation(value = "以年为单位，列出所有竞答活动，每次的参与人数、使用试题数量和活动时间（开始时间）[名称：全年竞答活动]")
    @RequestMapping(value = "/statistics/getStatisticsExPQCount", method = RequestMethod.POST)
    public List<Map<String,Object>> getStatisticsExPQCount(@RequestBody ExamStatisticsDto examDTO){
        return this.examFeign.getStatisticsExPQCount(examDTO);
    };



}
