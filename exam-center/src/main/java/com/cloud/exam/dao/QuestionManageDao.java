package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.exam.model.exam.QuestionManage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


@Mapper
public interface QuestionManageDao extends BaseMapper<QuestionManage> {

    List<QuestionManage> findAll(Page page, Map<String, Object> params);

    IPage<QuestionManage> findAllTest(Page page);

    @Select("select * from question_manage t where t.type = #{type} and t.kp_id = #{kpId} and t.direct_id=#{directId} ")
    List<QuestionManage> findQuestionEquals(QuestionManage question);

    List<QuestionManage> getQuestionIn(@Param("type") int type,
                                       @Param("kpString") List<String> kpString,
                                       @Param("substring") List<Long> substring,
                                       @Param("qIds") List<Long> qIds,
                                       @Param("pdTypes") List<String> pdTypes,
                                       @Param("diff") Double diff);

    /**
     * 查询最后一条questionId
     *
     * @return FillQuestion
     */
    @Select("select questionId from question_manage order by questionId desc limit 1")
    QuestionManage findOnlyQuestionId();

    @Delete("delete from question_manage t where t.id=#{id} and t.status=#{status}")
    int deleteQuestion(QuestionManage question);

    @Select(" select * from question_manage t where t.code = #{code} ORDER BY \"version\" desc LIMIT 1")
    QuestionManage findByHighVersion(String code);

    /**
     * 根据情报方向查询问题
     *
     * @param id
     * @return
     */
    @Select("select * from question_manage t where t.direct_id=#{id}")
    List<QuestionManage> selectByDirectId(Long id);

    @Select("select score from paper_manage_rel pr where pr.paper_id = #{paperId} and pr.question_id = #{questionId}")
    Double getQuestionScoreById(@Param("paperId") Long paperId, @Param("questionId") Long questionId);


    List<QuestionManage> getQuestions(@Param("kpIds") List<Long> kpIds, @Param("intDirectIds") List<Long> intDirectIds, @Param("questionType") Integer questionType, @Param("questionNum") Integer questionNum, @Param("difficulty1") Double difficulty1, @Param("difficulty2") Double difficulty2, @Param("qIds") List<Long> qIds);

    /**
     * 查询所有的试题题目
     *
     * @return
     */
    List<QuestionManage> getQuestionList();

    /**
     * 试题统计查询
     *
     * @return
     */
    List<Map> getQuestionStatisticsByType();

    List<Map> getQuestionStatisticsByTypeAndDifficulty();

    List<Map> getQuestionStatisticsByIsUsed();

    IPage<QuestionManage> selectQuestionPageDM(IPage<QuestionManage> page, @Param(Constants.WRAPPER) Wrapper<QuestionManage> queryWrapper);

    IPage<QuestionManage> selectQuestionPage(IPage<QuestionManage> page, @Param(Constants.WRAPPER) Wrapper<QuestionManage> queryWrapper);

    /**
     * @author:胡立涛
     * @description: TODO 根据活动id查询参赛人员
     * @date: 2022/9/14
     * @param: [map]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    List<Map<String, Object>> getStudensByExamId(Map<String, Object> map);
}
