package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.exam.model.eval.ExamEvalPerson;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author:胡立涛
 * @description: TODO
 * @date: 2021/12/22
 * @param:
 * @return:
 */
@Mapper
public interface EvalPersonDao extends BaseMapper<ExamEvalPerson> {

    /**
     * @author:胡立涛
     * @description: TODO 单次训练：个人能力详情
     * @date: 2021/12/28
     * @param: [map]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    List<Map<String, Object>> getExamPersonalDetail(Map<String, Object> map);


    /**
     * @author:胡立涛
     * @description: TODO 查询 A 活动下的 A 部门下的人员总数
     * @date: 2021/12/28
     * @param: [map]
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     */
    Map<String, Object> getUserCountDept(Map<String, Object> map);


    /**
     * @author:胡立涛
     * @description: TODO 查询 A 活动下的 A 部门下的 各人员综合能力
     * @date: 2021/12/28
     * @param: [map]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    List<Map<String, Object>> getExamPersonalEval(Map<String, Object> map);


    /**
     *
     * @author:胡立涛
     * @description: TODO 查询该活动下所有考生信息
     * @date: 2021/12/29
     * @param: [examId]
     * @return: java.util.List<java.lang.Long>
     */
    List<Map<String,Object>> queryExam(@Param("examId") Long examId);


    List<Map<String,Object>> getPaperInfo(Map<String,Object> map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 查询题目包含的知识点
     * @date: 2021/12/29
     * @param: [map]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String,Object>> getKpByQuestId(Map<String,Object> map);



    /**
     *
     * @author:胡立涛
     * @description: TODO 根据参数查询个人能力信息
     * @date: 2021/12/30
     * @param: [map]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String,Object>> getEvalPersonInfo(Map<String,Object> map);



    /**
     *
     * @author:胡立涛
     * @description: TODO 参加本次活动的部门
     * @date: 2021/12/30
     * @param: [map]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String,Object>> getDeptByAcId(Map<String,Object> map);


    /**
     *
     * @author:胡立涛
     * @description: TODO 本活动的部门能力
     * @date: 2021/12/30
     * @param: [map]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String,Object>> getEvalDeptScore(Map<String,Object> map);



}
