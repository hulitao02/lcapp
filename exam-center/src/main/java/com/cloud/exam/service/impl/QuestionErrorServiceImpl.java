package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.QuestionErrorDao;
import com.cloud.exam.model.exam.QuestionError;
import com.cloud.exam.service.QuestionErrorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author meidan
 */
@Service
public class QuestionErrorServiceImpl extends ServiceImpl<QuestionErrorDao, QuestionError> implements QuestionErrorService {

    @Autowired
    private QuestionErrorDao questionErrorDao;
    @Override
    public IPage<QuestionError> findAll(Page<QuestionError> page, Map<String,Object> params) {
        QueryWrapper<QuestionError> questionQueryWrapper=new QueryWrapper<>();
        /*if ( null != params.get("id").toString() && "" != params.get("id").toString()){
            questionQueryWrapper.eq("id",Long.parseLong(params.get("id").toString()));
        }*/
        if(null != params.get("question") && ""!=params.get("question").toString() ){
            questionQueryWrapper.like("question",params.get("question").toString());
        }
        if (null != params.get("type") && ""!=params.get("type").toString()){
            questionQueryWrapper.eq("type",Integer.parseInt(params.get("type").toString()));
        }
        questionQueryWrapper.select().orderByDesc("create_time");
        return questionErrorDao.selectPage(page,questionQueryWrapper);
    }

    /**
     * @author: 胡立涛
     * @description: TODO 知识点删除
     * @date: 2022/5/23
     * @param: [kpIds]
     * @return: void
     */
    @Override
    @Transactional
    public void delKnowledgePoint(Long[] kpIds) {
        questionErrorDao.delQuestionError(kpIds);
        questionErrorDao.delExamEvalPerson(kpIds);
        questionErrorDao.delExamEvalPersonHis(kpIds);
        questionErrorDao.delExamEvalDept(kpIds);
        questionErrorDao.delExamEvalDeptHis(kpIds);
    }


    /**
     * @author: 胡立涛
     * @description: TODO 知识删除
     * @date: 2022/5/25
     * @param: [kIds]
     * @return: void
     */
    @Override
    @Transactional
    public void delKnowledge(String[] kIds) {
        // 根据知识查询课程id
        List<Map> courseIds = questionErrorDao.getCourseIds(kIds);
        if (courseIds != null && courseIds.size() > 0) {
            Long[] cIds = new Long[courseIds.size()];
            for (int i = 0; i < courseIds.size(); i++) {
                Map map = courseIds.get(i);
                cIds[i] = Long.valueOf(map.get("id").toString());
            }
            // 课程与知识关联表与该知识相关数据进行删除（course_kp_rel）
            questionErrorDao.delCourseKRel(kIds);
            // 课程信息表与该知识点相关联数据进行删除（course_study）
            questionErrorDao.delCourseStudy(cIds);
            // 课程与学生关系表与该知识点关联数据进行删除（course_user_rel）
            questionErrorDao.delCourseUserRel(cIds);
            // 课程与试题关系表与该知识点关联数据进行删除（course_question_rel）
            questionErrorDao.delCourseQuestionRel(cIds);
            // 课后练习学生答案表与该知识点关联数据进行删除（course_student_answer）
            questionErrorDao.delCourseStudentAnswer(cIds);
        }
    }
}
