package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.exam.model.eval.ExamEvalDept;
import org.apache.ibatis.annotations.Mapper;

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
public interface EvalDeptDao extends BaseMapper<ExamEvalDept> {

    /**
     * @author:胡立涛
     * @description: TODO 单次训练：各部门综合综合能力
     * @date: 2021/12/28
     * @param: [map]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    List<Map<String, Object>> getExamDeptEval(Map<String, Object> map);

    /**
     * @author:胡立涛
     * @description: TODO 查询 A活动 下 A部门下的所有知识点
     * @date: 2021/12/28
     * @param: [map]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    List<Map<String, Object>> getKpIdsForDept(Map<String, Object> map);


    /**
     * @author:胡立涛
     * @description: TODO 单次：部门人员能力详情
     * @date: 2021/12/29
     * @param: [map]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    List<Map<String, Object>> getExamDeptDetail(Map<String, Object> map);


    /**
     *
     * @author:胡立涛
     * @description: TODO 本次活动 各部门综合能力值
     * @date: 2021/12/30
     * @param: [map]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> getExamDeptScore(Map<String, Object> map);



}
