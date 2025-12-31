package com.cloud.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.exam.dao.ExamEvalDeptDao;
import com.cloud.exam.dao.ExamEvalPersonHisDao;
import com.cloud.exam.model.course.CourseKpRel;
import com.cloud.exam.model.eval.ExamEvalDept;
import com.cloud.exam.model.eval.ExamEvalDeptHis;
import com.cloud.exam.model.eval.ExamEvalPerson;
import com.cloud.exam.model.eval.ExamEvalPersonHis;
import com.cloud.exam.model.exam.QuestionError;
import com.cloud.exam.model.exam.QuestionKpRel;
import com.cloud.exam.service.*;
import com.cloud.feign.managebackend.ManageBackendFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RefreshScope
public class QuestionKpController {

    @Autowired
    QuestionKpRelService questionKpRelService;
    @Autowired
    QuestionErrorService questionErrorService;
    @Autowired
    ExamEvalPersonService examEvalPersonService;
    @Autowired
    ExamEvalPersonHisService examEvalPersonHisService;
    @Autowired
    ExamEvalDeptService examEvalDeptService;
    @Autowired
    ExamEvalDeptHisService examEvalDeptHisService;
    @Autowired
    CourseKpRelService courseKpRelService;
    @Autowired
    ManageBackendFeign manageBackendFeign;


    /**
     * @author:胡立涛
     * @description: TODO 查看知识点是否被试题使用
     * @date: 2022/1/25
     * @param: [kpId]
     * @return: int 1：使用 0：未使用
     */
    @GetMapping("/checkKpId/{kpId}")
    public int checkKpId(@PathVariable("kpId") Long kpId) {
        QueryWrapper<QuestionKpRel> queryWrapper = new QueryWrapper();
        queryWrapper.eq("kp_id", kpId);
        List list = questionKpRelService.list(queryWrapper);
        if (list != null && list.size() > 0) {
            return 1;
        }
        return 0;
    }


    /**
     *
     * @author: 胡立涛
     * @description: TODO 查看知识点是否关联错误试题
     * @date: 2022/5/16
     * @param: [kpId]
     * @return: int 1：使用 0：未使用
     */
    @GetMapping("/checkErrorKpId/{kpId}")
    public int checkErrorKpId(@PathVariable("kpId") Long kpId) {
        QueryWrapper<QuestionError> queryWrapper = new QueryWrapper();
        queryWrapper.eq("kp_id", kpId);
        List list =questionErrorService.list(queryWrapper);
        if (list != null && list.size() > 0) {
            return 1;
        }
        return 0;
    }


    /**
     *
     * @author: 胡立涛
     * @description: TODO 校验用户每次活动能力评估是否使用该知识点
     * @date: 2022/5/16
     * @param: [kpId]
     * @return: int 1:使用 0：未使用
     */
    @GetMapping("/checkExamEvalPerson/{kpId}")
    public int checkExamEvalPerson(@PathVariable("kpId") Long kpId) {
        QueryWrapper<ExamEvalPerson> queryWrapper = new QueryWrapper();
        queryWrapper.eq("kp_id", kpId);
        List list =examEvalPersonService.list(queryWrapper);
        if (list != null && list.size() > 0) {
            return 1;
        }
        return 0;
    }



    /**
     *
     * @author: 胡立涛
     * @description: TODO 校验用户历史活动能力评估是否使用该知识点
     * @date: 2022/5/16
     * @param: [kpId]
     * @return: int 1:使用 0：未使用
     */
    @GetMapping("/checkExamEvalPersonHis/{kpId}")
    public int checkExamEvalPersonHis(@PathVariable("kpId") Long kpId) {
        QueryWrapper<ExamEvalPersonHis> queryWrapper = new QueryWrapper();
        queryWrapper.eq("kp_id", kpId);
        List list =examEvalPersonHisService.list(queryWrapper);
        if (list != null && list.size() > 0) {
            return 1;
        }
        return 0;
    }


    /**
     *
     * @author: 胡立涛
     * @description: TODO 校验部门单次活动能力评估是否使用该知识点
     * @date: 2022/5/24
     * @param: [kpId]
     * @return: int
     */
    @GetMapping("/checkExamEvalDept/{kpId}")
    public int checkExamEvalDept(@PathVariable("kpId") Long kpId) {
        QueryWrapper<ExamEvalDept> queryWrapper = new QueryWrapper();
        queryWrapper.eq("kp_id", kpId);
        List list =examEvalDeptService.list(queryWrapper);
        if (list != null && list.size() > 0) {
            return 1;
        }
        return 0;
    }


    /**
     *
     * @author: 胡立涛
     * @description: TODO 校验部门历史活动能力评估是否使用该知识点
     * @date: 2022/5/24
     * @param: [kpId]
     * @return: int
     */
    @GetMapping("/checkExamEvalDeptHis/{kpId}")
    public int checkExamEvalDeptHis(@PathVariable("kpId") Long kpId) {
        QueryWrapper<ExamEvalDeptHis> queryWrapper = new QueryWrapper();
        queryWrapper.eq("kp_id", kpId);
        List list =examEvalDeptHisService.list(queryWrapper);
        if (list != null && list.size() > 0) {
            return 1;
        }
        return 0;
    }


    /**
     *
     * @author: 胡立涛
     * @description: TODO 验证课程是否与知识关联
     * @date: 2022/5/24
     * @param: [knowledgeId]
     * @return: int
     */
    @GetMapping("/checkCourseKpRel/{knowledgeCode}")
    public int checkCourseKpRel(@PathVariable("knowledgeCode") String knowledgeCode) {
        QueryWrapper<CourseKpRel> queryWrapper = new QueryWrapper();
        queryWrapper.eq("senses_id", knowledgeCode);
        List list =courseKpRelService.list(queryWrapper);
        if (list != null && list.size() > 0) {
            return 1;
        }
        return 0;
    }


    /**
     * @author: 胡立涛
     * @description: TODO 知识点删除
     * @date: 2022/5/23
     * @param: [kpIds]
     * @return: void
     */
    @PostMapping(value = "/delKnowledgePoint")
    public void delKnowledgePoint(@RequestParam Map<String,Object> map) {
        String kpCodeStr=map.get("kpCodeStr").toString();
        kpCodeStr=kpCodeStr.substring(0,kpCodeStr.length()-1);
        String[] kpCodeArr=kpCodeStr.split(",");
        Long[] kpIds = new Long[kpCodeArr.length];
        for (int i=0;i<kpCodeArr.length;i++){
            String kpCode=kpCodeArr[i];
            // 根据知识点code查询知识点id
            Map knowledgePointsByCode=manageBackendFeign.getKnowledgePointsByCode(kpCode);
            if (knowledgePointsByCode == null) {
                kpIds[i] = -1L;
            } else {
                long kpId = Long.valueOf(knowledgePointsByCode.get("id").toString());
                kpIds[i] = kpId;
            }
        }
        questionErrorService.delKnowledgePoint(kpIds);
    }



    /**
     * @author: 胡立涛
     * @description: TODO 删除课程中的知识
     * @date: 2022/5/28
     * @param: [kIds]
     * @return: void
     */
    @PostMapping(value = "/delKnowledge")
    public void delKnowledge(@RequestParam Map<String, Object> map) {
        String kpIdsStr = map.get("kpIdsStr").toString();
        kpIdsStr = kpIdsStr.substring(0, kpIdsStr.length() - 1);
        String[] idArr = kpIdsStr.split(",");
        String[] kIds = new String[idArr.length];
        for (int i = 0; i < idArr.length; i++) {
            kIds[i] = "'" + idArr[i] + "'";
        }
        questionErrorService.delKnowledge(kIds);
    }

}
