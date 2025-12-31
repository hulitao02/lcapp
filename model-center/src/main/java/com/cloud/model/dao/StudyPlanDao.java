package com.cloud.model.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.model.bean.vo.PlanVO;
import com.cloud.model.bean.vo.StudyPlanVo;
import com.cloud.model.model.StudyPlan;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface StudyPlanDao extends BaseMapper<StudyPlan> {

    List<StudyPlan> getStudyPlanInfos(Map<String, Object> paramsMap);

    List<StudyPlanVo> getStudyPlansWithStudyStatus(Map<String, Object> paramsMap);

    List<Map<String,Object>> getStatisticsStudyPlansWithStudyStatus(Map<String,Object> paramMap);


    List<PlanVO> getPlanKnowledgeList(Map<String,Object> paramMap);

}
