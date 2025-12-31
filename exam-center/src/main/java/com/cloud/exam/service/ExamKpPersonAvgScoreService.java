package com.cloud.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.exam.model.ExamKpPersonAvgScore;
import com.cloud.exam.model.eval.*;

import java.util.List;

/**
 * <p>
 * 学员的知识点平均成绩 服务类
 * </p>
 *
 * @author tongkesong
 * @since 2023-09-09
 */
public interface ExamKpPersonAvgScoreService extends IService<ExamKpPersonAvgScore> {

    List<ExamKpPersonAvgScore> calculate(Long examId);

    List<EvalKpNewDto> getPersonalAbility(Long[] kpIds);

    PersonalHistoryDto getPersonalHisScore(Integer year, Integer month);

    List<TrainScheduleDto> trainSchedule(Integer year);

    List<EvalKpNewDto> getDeptKpScore(Long[] kpIds);

    DeptAbilityDto deptAbilityBak(Long[] kpIds, Integer Limit);

    DeptAbilityNewDto deptAbility(Long[] kpIds, Integer Limit);

    List<EvalDeptNewDto> distribution(Long kpId);

    EvalDto getPersonalScore();

    EvalKpDto getDeptScore();

    List<EvalDeptNewDto> getPersonalDeptScore(Long kpId);
}
