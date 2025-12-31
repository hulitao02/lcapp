package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.exam.model.eval.ExamEvalPersonHis;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;


@Mapper
public interface EvalPersonHisDao extends BaseMapper<ExamEvalPersonHis> {

    /**
     * @author:胡立涛
     * @description: TODO 获取月份
     * @date: 2021/12/22
     * @param: [map]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    List<Map<String, Object>> getMonth(Map<String, Object> map);

    /**
     * @author:胡立涛
     * @description: TODO 根据月份，知识点id获取得分
     * @date: 2021/12/22
     * @param: [map]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    Map<String, Object> getEvalPersonHisInfo(Map<String, Object> map);

    /**
     * @author:胡立涛
     * @description: TODO 个人 能力分布
     * @date: 2021/12/22
     * @param: [map]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    List<Map<String, Object>> getAbility(Map<String, Object> map);


    /**
     * @author:胡立涛
     * @description: TODO 个人综合能力评分
     * @date: 2021/12/22
     * @param: [map]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    Map<String, Object> getPersonalScore(Map<String, Object> map);

    /**
     * @author:胡立涛
     * @description: TODO 获取当前部门下的人员
     * @date: 2021/12/23
     * @param: [map]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    List<Map<String, Object>> getUsersByDeptId(Map<String, Object> map);

    Map<String, Object> getInfoByPar(Map<String, Object> map);

    /**
     * @author:胡立涛
     * @description: TODO 成员能力分布
     * @date: 2021/12/29
     * @param: [map]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    List<Map<String, Object>> getDistribution(Map<String, Object> map);


    /**
     *
     * @author:胡立涛
     * @description: TODO 根据user_id,kp_id更新cur_flg=0
     * @date: 2021/12/30
     * @param: [map]
     * @return: void
     */
    void updateInfo(Map<String, Object> map);


    /**
     *
     * @author:胡立涛
     * @description: TODO 计算各用户的历史综合能力
     * @date: 2021/12/30
     * @param: [map]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> getPersonHisScore(Map<String,Object> map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 查询个人能力信息
     * @date: 2021/12/30
     * @param: [map]
     * @return: java.util.Map<java.lang.String,java.lang.Object>
     */
    Map<String, Object> getPersonHisPar(Map<String,Object> map);


    /**
     *
     * @author:胡立涛
     * @description: TODO 根据id更新综合能力
     * @date: 2021/12/30
     * @param: [map]
     * @return: void
     */
    void updateHisZonghe(Map<String,Object> map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 根据id更新cur_flg=0
     * @date: 2021/12/30
     * @param: [map]
     * @return: void
     */
    void updateFlgById(Map<String,Object> map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 本次考试后，部门各知识点能力计算 exam_eval_dept_his
     * @date: 2021/12/31
     * @param: [map]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> getDeptHis(Map<String,Object> map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 获取A知识点能力最好的前N名人员
     * @date: 2022/1/4
     * @param: [map]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> getTopUser(Map<String,Object> map);


    /**
     *
     * @author: 胡立涛
     * @description: TODO 根据阀值，查询低于该阀值的知识点
     * @date: 2022/5/30
     * @param: [map]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String,Object>> getPointList(Map<String,Object> map);

}
