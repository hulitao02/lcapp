package com.cloud.model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.model.bean.vo.PlanVO;
import com.cloud.model.bean.vo.StudyPlanVo;
import com.cloud.model.dao.StudyPlanDao;
import com.cloud.model.model.StudyKnowledge;
import com.cloud.model.model.StudyPlan;
import com.cloud.model.service.StudyKnowledgeService;
import com.cloud.model.service.StudyPlanService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class StudyPlanServiceImpl extends ServiceImpl<StudyPlanDao, StudyPlan>
        implements StudyPlanService {

    @Autowired
    private StudyPlanDao studyPlanDao;

    @Autowired
    private StudyKnowledgeService studyKnowledgeService;

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void saveStudyPlanAndKpList(StudyPlan studyPlan) throws Exception {
        if (Objects.isNull(studyPlan)) {
            throw new Exception(String.format("用户:{%s},创建的学习计划为空"));
        }
        this.saveOrUpdate(studyPlan);
        /**
         *  关联的知识点，之前的全部更新成 0
         */
        QueryWrapper<StudyKnowledge> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("plan_id", studyPlan.getId());
        List<StudyKnowledge> dbKnList = this.studyKnowledgeService.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(dbKnList)) {
            dbKnList.stream().forEach(kn -> {
                kn.setStatus(0);
                kn.setUpdateTime(new Date());
            });
            this.studyKnowledgeService.saveOrUpdateBatch(dbKnList);
        }
        /**
         *  保存最新的
         */
        List<StudyKnowledge> studyKnowledgeList = studyPlan.getStudyKnowledgeList();
        if (CollectionUtils.isNotEmpty(studyKnowledgeList)) {
            studyKnowledgeList.stream().forEach(kn -> {
                if (Objects.nonNull(studyPlan.getId())) {
                    kn.setPlanId(studyPlan.getId());
                }
                if (Objects.isNull(studyPlan.getStatus())) {
                    kn.setStatus(1);
                }
                kn.setUpdateTime(new Date());
            });
            this.studyKnowledgeService.saveOrUpdateBatch(studyKnowledgeList);
        }
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void deleteStudyPlanAndKpList(StudyPlan studyPlan) throws Exception {
        studyPlan.setStatus(0);
        studyPlan.setUpdateTime(new Date());
        this.saveOrUpdate(studyPlan);
        QueryWrapper<StudyKnowledge> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("plan_id", studyPlan.getId());
        queryWrapper.eq("status", 1);
        List<StudyKnowledge> knowledgeList = this.studyKnowledgeService.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(knowledgeList)) {
            knowledgeList.stream().forEach(kn -> {
                kn.setStatus(0);
                kn.setUpdateTime(new Date());
            });
            boolean b = this.studyKnowledgeService.saveOrUpdateBatch(knowledgeList);
        }
    }

    @Override
    public List<StudyPlan> getStudyPlanInfos(Integer userId, Date paramStudyStartTime, Date paramStudyEndTime) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("status", 1);
        paramsMap.put("userId", userId);
        paramsMap.put("paramStudyStartTime", paramStudyStartTime);
        paramsMap.put("paramStudyEndTime", paramStudyEndTime);
        List<StudyPlan> studyPlanInfos = this.studyPlanDao.getStudyPlanInfos(paramsMap);
        return studyPlanInfos;
    }

    @Override
    public List<StudyPlanVo> getStudyPlansWithStudyStatus(Map<String,Object> paramsMap) {

        List<StudyPlanVo> studyPlanInfos = this.studyPlanDao.getStudyPlansWithStudyStatus(paramsMap);
        return studyPlanInfos;
    }

    @Override
    public List<Map<String, Object>> getStatisticsStudyPlansWithStudyStatus(Map<String, Object> paramMap) {
        return this.studyPlanDao.getStatisticsStudyPlansWithStudyStatus(paramMap);
    }

    @Override
    public List<PlanVO> getPlanKnowledgeList(Map<String, Object> paramMap) {
        return this.studyPlanDao.getPlanKnowledgeList(paramMap);
    }

}
