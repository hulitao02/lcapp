package com.cloud.model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.bean.vo.PlanVO;
import com.cloud.model.bean.vo.StudyPlanVo;
import com.cloud.model.model.StudyPlan;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface StudyPlanService extends IService<StudyPlan> {

    void saveStudyPlanAndKpList(StudyPlan studyPlan) throws Exception;

    void deleteStudyPlanAndKpList(StudyPlan studyPlan) throws Exception;
    /**
     *  查询用户的学习计划
     * @param userId
     * @param paramStudyStartTime
     * @param paramStudyEndTime
     * @return
     */
    List<StudyPlan> getStudyPlanInfos(Integer userId , Date paramStudyStartTime, Date paramStudyEndTime);


    public List<StudyPlanVo> getStudyPlansWithStudyStatus(Map<String,Object> paramMap);


    /**
     *  每个月 计划完成情况统计
     * @param paramMap
     * @return
     */
    List<Map<String,Object>> getStatisticsStudyPlansWithStudyStatus(Map<String,Object> paramMap);

    /**
     *  根据人员和学习计划 找到相关的知识
     * @param paramMap
     * @return
     */
    List<PlanVO> getPlanKnowledgeList(Map<String,Object> paramMap);






}
