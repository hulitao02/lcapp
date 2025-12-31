package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.exam.model.eval.ExamEvalDeptHis;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface EvalDeptHisDao extends BaseMapper<ExamEvalDeptHis> {


    /**
     *
     * @author:胡立涛
     * @description: TODO 单位综合能力
     * @date: 2021/12/23
     * @param: [map]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String,Object>> getDeptKpScore(Map<String,Object> map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 单位综合能力分数
     * @date: 2021/12/23
     * @param: [map]
     * @return: java.util.Map<java.lang.String,java.lang.Object>
     */
    Map<String,Object> getDeptScore(Map<String,Object> map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 获取单位等分高的前n个知识点
     * @date: 2021/12/29
     * @param: [map]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String,Object>> getTopKpName(Map<String,Object> map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 删除部门能力 exam_eval_dept_his
     * @date: 2021/12/31
     * @param: [map]
     * @return: void
     */
    void delDeptHis(Map<String,Object> map);
}
