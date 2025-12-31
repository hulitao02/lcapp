package com.cloud.feign.exam;


import com.cloud.config.FeignConfig;
import com.cloud.model.exam.ExamStatisticsDto;
import com.cloud.model.user.AppUser;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;


@Component
@FeignClient(name = "exam-center", configuration = {FeignConfig.class})
public interface ExamFeign {


    @PostMapping("/weboffice/uploadFile")
    public Map<String, String> upload(@RequestPart("upload") MultipartFile file, @RequestParam("fileMd5") String fileMd5);

    @GetMapping("/statistics/groupbyType")
    public List<Map> getQuestionStatisticsByType();


    @GetMapping("/statistics/groupbyTypeAndDifficulty")
    public List<Map> getQuestionStatisticsByTypeAndDifficulty();


    @GetMapping("/statistics/questionsIsUsed")
    public List<Map> getQuestionStatisticsByIsUsed();


    /**
     * 以年为单位，统计每个月开展训练任务数量、参加训练人数。【名称：各月训练人员数量和参训人数统计
     *
     * @param examStatisticsDto
     * @return
     */
    @RequestMapping(value = "/statistics/getStatisticsExamCountAndUserCount", method = RequestMethod.POST)
    public List<Map> getStatisticsExamCountAndUserCount(@RequestBody ExamStatisticsDto examStatisticsDto);

    /**
     * 以年为单位，排名自测数量最多的前10个学员，展示姓名、自测次数和自测题数
     *
     * @param examStatisticsDto
     * @return
     */
    @RequestMapping(value = "/statistics/getStatisticsUsersExamAndQuestionCount", method = RequestMethod.POST)
    public Map<String, Object> getStatisticsUsersExamAndQuestionCount(@RequestBody ExamStatisticsDto examStatisticsDto);

    /**
     * 正在自测的人员
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/statistics/getStatisticsExamingUserList", method = RequestMethod.POST)
    public List<Map> getStatisticsExamingUserList(@RequestBody ExamStatisticsDto examStatisticsDto);

    /**
     * 查询正在有人参加的活动集合列表
     * examStatisticsDto
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/statistics/getStatisticsExamListByTypeAndStatus", method = RequestMethod.POST)
    public List<Map> getStatisticsExamListByTypeAndStatus(@RequestBody ExamStatisticsDto examStatisticsDto);

    /**
     * 查询正在参加考试的人员信息
     * examStatisticsDto
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/statistics/getUserListByExamId", method = RequestMethod.POST)
    public List<AppUser> getExamingUserList(@RequestBody ExamStatisticsDto examStatisticsDto);


    @ApiOperation(value = "以年为单位，统计每个月在线考核试卷创建数量和使用数量")
    @RequestMapping(value = "/statistics/getStatisticsPaperCount", method = RequestMethod.POST)
    public List<Map> getStatisticsPaperCount(@RequestBody ExamStatisticsDto examDTO);

    /**
     * 以年为单位，统计每个月在线考核活动数量、使用试卷数量（被活动关联的）、参考人数")
     *
     * @param examDTO
     * @return
     */
    @ApiOperation(value = "以年为单位，统计每个月在线考核活动数量、使用试卷数量（被活动关联的）、参考人数")
    @RequestMapping(value = "/statistics/getStatisticsExPUCount", method = RequestMethod.POST)
    public Map<String, List<String>> getStatisticsExPUCount(@RequestBody ExamStatisticsDto examDTO);

    /**
     * 以年为单位，统计每个月在线考核活动数量、使用试卷数量（被活动关联的）、参考人数")
     *
     * @param examDTO
     * @return
     */
    @ApiOperation(value = "以年为单位，列出所有竞答活动，每次的参与人数、使用试题数量和活动时间（开始时间）")
    @RequestMapping(value = "/statistics/getStatisticsExPQCount", method = RequestMethod.POST)
    public List<Map<String, Object>> getStatisticsExPQCount(@RequestBody ExamStatisticsDto examDTO);

    /**
     * @author:胡立涛
     * @description: TODO 查看知识点是否绑定试题 1：绑定 0：未绑定
     * @date: 2022/1/25
     * @param: [kpId]
     * @return: int
     */
    @GetMapping("/checkKpId/{kpId}")
    int checkKpId(@PathVariable("kpId") Long kpId);


    /**
     * @author: 胡立涛
     * @description: TODO 查看知识点是否被错误试题使用
     * @date: 2022/5/16
     * @param: [kpId]
     * @return: int 1：使用 0：未使用
     */
    @GetMapping("/checkErrorKpId/{kpId}")
    int checkErrorKpId(@PathVariable("kpId") Long kpId);


    /**
     * @author: 胡立涛
     * @description: TODO 校验用户每次活动能力评估是否使用该知识点
     * @date: 2022/5/16
     * @param: [kpId]
     * @return: int 1：使用 0：未使用
     */
    @GetMapping("/checkExamEvalPerson/{kpId}")
    int checkExamEvalPerson(@PathVariable("kpId") Long kpId);

    /**
     * @author: 胡立涛
     * @description: TODO 校验用户历史活动能力评估是否使用该知识点
     * @date: 2022/5/20
     * @param: [kpId]
     * @return: int 1：使用 0：未使用
     */
    @GetMapping("/checkExamEvalPersonHis/{kpId}")
    int checkExamEvalPersonHis(@PathVariable("kpId") Long kpId);


    /**
     * @author: 胡立涛
     * @description: TODO 校验部门每次活动能力评估是否使用该知识点
     * @date: 2022/5/20
     * @param: [kpId]
     * @return: int
     */
    @GetMapping("/checkExamEvalDept/{kpId}")
    int checkExamEvalDept(@PathVariable("kpId") Long kpId);


    /**
     * @author: 胡立涛
     * @description: TODO 校验部门历史活动能力评估是否使用该知识点
     * @date: 2022/5/20
     * @param: [kpId]
     * @return: int
     */
    @GetMapping("/checkExamEvalDeptHis/{kpId}")
    int checkExamEvalDeptHis(@PathVariable("kpId") Long kpId);


    /**
     * @author: 胡立涛
     * @description: TODO 校验课程是否与知识关联
     * @date: 2022/5/24
     * @param: [knowledgeId]
     * @return: int
     */
    @GetMapping("/checkCourseKpRel/{knowledgeCode}")
    int checkCourseKpRel(@PathVariable("knowledgeCode") String knowledgeCode);


    /**
     * @author: 胡立涛
     * @description: TODO 知识点删除
     * @date: 2022/5/23
     * @param: [kpIds]
     * @return: int
     */
    @PostMapping(value = "/delKnowledgePoint")
    void delKnowledgePoint(@RequestParam Map<String, Object> map) throws Exception;


    /**
     * @author: 胡立涛
     * @description: TODO 删除课程中的知识
     * @date: 2022/5/28
     * @param: [kIds]
     * @return: void
     */
    @PostMapping(value = "/delKnowledge")
    void delKnowledge(@RequestParam Map<String, Object> map) throws Exception;


    /**
     * @author: 胡立涛
     * @description: TODO 根据能力评估值设定的阀值，查询低于该阀值的知识点列表
     * @date: 2022/5/30
     * @param: [evalScore]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    @PostMapping(value = "getPointList")
    List<Map<String, Object>> getPointList(@RequestBody double evalScore) throws Exception;

    /**
     * 根据试卷id查询试卷试题包含的所有知识点
     *
     * @param paperId
     * @return Set<Long>
     */
    @RequestMapping(value = "getKpIdsByPaperId", method = RequestMethod.GET)
    public Set<Long> getKpIdsByPaperId(@RequestParam("paperId") Long paperId);

    /**
     * 根据课程id获取课程关联的知识点
     *
     * @param courseId
     * @return
     */
    @RequestMapping(value = "getKpByCourseId", method = RequestMethod.GET)
    public Set<Long> getKpByCourseId(@RequestParam("courseId") Long courseId);

    /**
     * 根据活动id和部门id或者type获取人员共同的知识点
     *
     * @param examId
     * @param departId
     * @param type
     * @return
     */
    @RequestMapping(value = "getKpListByExamAndType", method = RequestMethod.GET)
    public Set<Long> getKpListByExamAndType(@RequestParam("examId") Long examId, @RequestParam("departId") Long departId, @RequestParam("type") Integer type);

    /**
     * @author:胡立涛
     * @description: TODO 知识智能推荐：获取个人能力评估_能力分布值
     * @date: 2024/9/20
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "pg/person/abilityzz")
    public List<Map> abilityzz(@RequestBody Map map);
}
