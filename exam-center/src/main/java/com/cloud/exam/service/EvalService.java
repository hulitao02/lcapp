package com.cloud.exam.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EvalService {

    public void makeEvaluation(long examId);

    public int getPersonalScore(Long userId, Long kpId);

    public List<Map<String, Object>> getPersonalAbility(Long userId, Long[] kpIds);
    
    public Map<Long, Integer> getPersonalQuestions(Long userId, Long[] kpIds);

    public List<Map<String, Object>> getPersonalHisScore(Long userId, Long[] kpIds);

    public List<Map<String, Object>> getPersonalDeptScore(Set<Long> userIds, Long kpId);
    
    public int getDeptScore(Long deptId, Long kpId);
    
    public List<Map<String, Object>> getDeptKpScore(Long deptId, Long[] kpIds);
    
    public List<Map<String, Object>> getDeptAbility(Long deptId, Long[] kpIds);
    
    public List<Map<String, Object>> getDeptDistribution(Long deptId, Long kpId);
    
    public Map<Long, Integer> getDeptQuestions(Long deptId, Long kpId);
    
    public List<Map<String, Object>> getExamDeptEval(Long examId, Long paperId);
    
    public Map<String, Object> getExamDeptScore(Long examId, Long paperId);
    
    public List<Map<String, Object>> getExamDeptDetail(Long examId, Long paperId, Long deptId);
    
    public List<Map<String, Object>> getExamPersonalEval(Long examId, Long paperId, Long deptId);
    
    public Map<Long, Double> getExamPersonalScore(Long examId, Long paperId, Long deptId);
    
    public List<Map<String, Object>> getExamPersonalDetail(Long examId, Long paperId, Long userId);
}
