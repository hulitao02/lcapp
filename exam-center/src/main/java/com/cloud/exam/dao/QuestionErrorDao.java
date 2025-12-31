package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.exam.model.exam.QuestionError;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


@Mapper
public interface QuestionErrorDao extends BaseMapper<QuestionError> {

    /**
     * @author: 胡立涛
     * @description: TODO 根据知识点删除错误试题信息
     * @date: 2022/5/23
     * @param: [kpId]
     * @return: void
     */
    void delQuestionError(@Param("kpIds") Long[] kpIds);

    /**
     * @author: 胡立涛
     * @description: TODO 删除单次个人能力评估
     * @date: 2022/5/23
     * @param: [kpId]
     * @return: void
     */
    void delExamEvalPerson(@Param("kpIds") Long[] kpIds);

    /**
     * @author: 胡立涛
     * @description: TODO 删除历史个人能力评估
     * @date: 2022/5/23
     * @param: [kpId]
     * @return: void
     */
    void delExamEvalPersonHis(@Param("kpIds") Long[] kpIds);


    /**
     * @author: 胡立涛
     * @description: TODO 删除单次部门能力评估
     * @date: 2022/5/23
     * @param: [kpId]
     * @return: void
     */
    void delExamEvalDept(@Param("kpIds") Long[] kpIds);

    /**
     * @author: 胡立涛
     * @description: TODO 删除历史部门能力评估
     * @date: 2022/5/23
     * @param: [kpId]
     * @return: void
     */
    void delExamEvalDeptHis(@Param("kpIds") Long[] kpIds);


    /**
     * @author: 胡立涛
     * @description: TODO 删除课程与知识相关数据
     * @date: 2022/5/25
     * @param: [kIds]
     * @return: void
     */
    void delCourseKRel(@Param("kIds") String[] kIds);

    /**
     * @author: 胡立涛
     * @description: TODO 根据知识查询课程id
     * @date: 2022/5/25
     * @param: [kIds]
     * @return: java.util.List<java.lang.Long>
     */
    List<Map> getCourseIds(@Param("kIds") String[] kIds);


    /**
     * @author: 胡立涛
     * @description: TODO 根据课程id删除courseStudy信息
     * @date: 2022/5/28
     * @param: [cIds]
     * @return: void
     */
    void delCourseStudy(@Param("cIds") Long[] cIds);

    /**
     * @author: 胡立涛
     * @description: TODO 根据课程id删除courseUserReal信息
     * @date: 2022/5/28
     * @param: [cIds]
     * @return: void
     */
    void delCourseUserRel(@Param("cIds") Long[] cIds);

    /**
     * @author: 胡立涛
     * @description: TODO 根据课程id删除courseQuestionRel信息
     * @date: 2022/5/28
     * @param: [cIds]
     * @return: void
     */
    void delCourseQuestionRel(@Param("cIds") Long[] cIds);


    /**
     * @author: 胡立涛
     * @description: TODO 根据课程id删除courseStudentAnswer信息
     * @date: 2022/5/28
     * @param: [cIds]
     * @return: void
     */
    void delCourseStudentAnswer(@Param("cIds") Long[] cIds);
}