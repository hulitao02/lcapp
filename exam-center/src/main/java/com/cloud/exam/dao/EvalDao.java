package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.exam.model.exam.Eval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhangsheng on 2021/06/25.
 */
@Mapper
public interface EvalDao extends BaseMapper<Eval> {

	//个人评估
    public Map<String, Object> getPersonalScore(@Param("userId") Long userId, @Param("month") Integer month, @Param("kpId") Long kpId);

    public Map<String, Object> getPersonalScore2(@Param("userId") Long userId, @Param("month") Integer month, @Param("lastMonth") Integer lastMonth, @Param("kpId") Long kpId);

    public List<Map<String, Object>> getPersonalAbility(@Param("userId") Long userId, @Param("month") Integer month, @Param("kpIds") Long[] kpIds);

    public List<Map<String, Object>> getPersonalAbility2(@Param("userId") Long userId, @Param("month") Integer month, @Param("lastMonth") Integer lastMonth, @Param("kpIds") Long[] kpIds);
    
    public List<Map<String, Object>> getPersonalQuestions(@Param("userId") Long userId, @Param("month") Integer month, @Param("kpIds") Long[] kpIds);

    public List<Map<String, Object>> getPersonalQuestions2(@Param("userId") Long userId, @Param("month") Integer month, @Param("lastMonth") Integer lastMonth, @Param("kpIds") Long[] kpIds);
    
    public List<Map<String, Object>> getPersonalHisScore(@Param("userId") Long userId, @Param("fromMonth") Integer fromMonth, @Param("kpIds") Long[] kpIds);
    
    public List<Map<String, Object>> getPersonalDeptScore(@Param("userIds") Set<Long> userIds, @Param("month") Integer month, @Param("kpId") Long kpId);

    public List<Map<String, Object>> getPersonalDeptScore2(@Param("userIds") Set<Long> userIds, @Param("month") Integer month, @Param("lastMonth") Integer lastMonth, @Param("kpId") Long kpId);
    
    @Select("select user_id from drawresult where ac_id= #{examId}")
    public List<Long> queryExamUserId(@Param("examId") Long examId);
    
    @Select("select paper_id, login_date from drawresult  where ac_id=#{examId} and user_id=#{userId}")
    public List<Map<String, Object>> queryExamUserPaperId(@Param("examId") Long examId, @Param("userId") Long userId);
    
    public List<Map<String, Object>> queryExamUserKpId(@Param("paperId") Long paperId, @Param("userId") Long userId);
    
    //团体评估
    public double getDeptScore(@Param("deptId") Long deptId, @Param("month") Integer month, @Param("kpId") Long kpId);
    
    public double getDeptScore2(@Param("deptId") Long deptId, @Param("month") Integer month, @Param("lastMonth") Integer lastMonth, @Param("kpId") Long kpId);
    
    public List<Map<String, Object>> getDeptKpScore(@Param("deptId") Long deptId, @Param("month") Integer month, @Param("kpIds") Long[] kpIds);
    
    public List<Map<String, Object>> getDeptKpScore2(@Param("deptId") Long deptId, @Param("month") Integer month, @Param("lastMonth") Integer lastMonth, @Param("kpIds") Long[] kpIds);
    
    public List<Map<String, Object>> getDeptAbility(@Param("deptId") Long deptId, @Param("month") Integer month, @Param("kpIds") Long[] kpIds);
    
    public List<Map<String, Object>> getDeptAbility2(@Param("deptId") Long deptId, @Param("month") Integer month, @Param("lastMonth") Integer lastMonth, @Param("kpIds") Long[] kpIds);
    
    public List<Map<String, Object>> getDeptDistribution(@Param("deptId") Long deptId, @Param("month") Integer month, @Param("kpId") Long kpId);
    
    public List<Map<String, Object>> getDeptDistribution2(@Param("deptId") Long deptId, @Param("month") Integer month, @Param("lastMonth") Integer lastMonth, @Param("kpId") Long kpId);
    
    public List<Map<String, Object>> getDeptQuestions(@Param("deptId") Long deptId, @Param("month") Integer month, @Param("kpId") Long kpId);
    
    public List<Map<String, Object>> getDeptQuestions2(@Param("deptId") Long deptId, @Param("month") Integer month, @Param("lastMonth") Integer lastMonth, @Param("kpId") Long kpId);

    //活动评估
    public List<Map<String, Object>> getExamDeptEval(@Param("examId") Long examId, @Param("paperId") Long paperId);
    
    public List<Map<String, Object>> getExamDeptScore(@Param("examId") Long examId, @Param("paperId") Long paperId);
    
    public List<Map<String, Object>> getExamDeptDetail(@Param("examId") Long examId, @Param("paperId") Long paperId, @Param("deptId") Long deptId);
    
    public List<Map<String, Object>> getExamPersonalEval(@Param("examId") Long examId, @Param("paperId") Long paperId, @Param("deptId") Long deptId);
    
    public List<Map<String, Object>> getExamPersonalScore(@Param("examId") Long examId, @Param("paperId") Long paperId, @Param("deptId") Long deptId);
    
    public List<Map<String, Object>> getExamPersonalDetail(@Param("examId") Long examId, @Param("paperId") Long paperId, @Param("userId") Long userId);
}
