package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.exam.model.ExamKpPersonAvgScore;
import com.cloud.exam.model.eval.EvalDeptNewDto;
import com.cloud.exam.model.eval.EvalKpNewDto;
import com.cloud.exam.model.eval.TrainScheduleDto;
import com.cloud.exam.model.eval.XAxisScoreDto;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 学员的知识点平均成绩 Mapper 接口
 * </p>
 *
 * @author tongkesong
 * @since 2023-09-09
 */
public interface ExamKpPersonAvgScoreDao extends BaseMapper<ExamKpPersonAvgScore> {
    List<ExamKpPersonAvgScore> analyzeExamKpPersonAvgScore(Long examId);

    List<EvalKpNewDto> getPersonalAbility(Long userId, Long[] kpIds);

    List<XAxisScoreDto> getPersonalHisScore(Long userId, Date startTime, Date endTime, boolean isMonth);

    List<TrainScheduleDto> trainSchedule(Long userId, int year);

    List<EvalKpNewDto> getDeptKpScore(List<Long> userIdList, Long[] kpIds);

    List<ExamKpPersonAvgScore> deptAbilityBak(Collection<Long> userIdList, Long[] kpIds);

    List<ExamKpPersonAvgScore> deptAbility(Collection<Long> userIdList, Long[] kpIds, Integer limit);

    List<EvalDeptNewDto> distribution(Collection<Long> userIdList, Long kpId);

    BigDecimal getPersonalScore(Long userId);

    BigDecimal getDeptScore(Collection<Long> userIdList);

    List<EvalDeptNewDto> getPersonalDeptScore(Collection<Long> userIdList, Long kpId);
}
