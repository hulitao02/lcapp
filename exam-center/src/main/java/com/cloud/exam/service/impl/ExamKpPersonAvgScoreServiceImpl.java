package com.cloud.exam.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.ExamDao;
import com.cloud.exam.dao.ExamKpPersonAvgScoreDao;
import com.cloud.exam.model.ExamKpPersonAvgScore;
import com.cloud.exam.model.eval.*;
import com.cloud.exam.model.exam.ExamDepartPaperRel;
import com.cloud.exam.service.ExamKpPersonAvgScoreService;
import com.cloud.exam.service.StudentAnswerService;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.user.AppUser;
import com.cloud.model.utils.AppUserUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <p>
 * 学员的知识点平均成绩 服务实现类
 * </p>
 *
 * @author tongkesong
 * @since 2023-09-09
 */
@Service
public class ExamKpPersonAvgScoreServiceImpl extends ServiceImpl<ExamKpPersonAvgScoreDao, ExamKpPersonAvgScore> implements ExamKpPersonAvgScoreService {
    @Resource
    private ExamDao examDao;
    @Resource
    private StudentAnswerService studentAnswerService;
    @Resource
    private ManageBackendFeign manageBackendFeign;
    @Resource
    private SysDepartmentFeign sysDepartmentFeign;


    @Override
    public List<ExamKpPersonAvgScore> calculate(Long examId) {
        List<ExamDepartPaperRel> examPaperRelList = examDao.getDepartAndPaperByExamId(examId);
        if (!CollectionUtils.isEmpty(examPaperRelList)) {
            Date now = new Date();
            Integer count = lambdaQuery().eq(ExamKpPersonAvgScore::getAcId, examId).count();
            if (count > 0) {
                return Collections.emptyList();
            }
            List<ExamKpPersonAvgScore> examKpPersonAvgScores = getBaseMapper().analyzeExamKpPersonAvgScore(examId);
            examKpPersonAvgScores.forEach(e -> e.setCreateTime(now));
            saveBatch(examKpPersonAvgScores);
            return examKpPersonAvgScores;
        }
        return null;
    }

    @Override
    public List<EvalKpNewDto> getPersonalAbility(Long[] kpIds) {
        List<EvalKpNewDto> evalKpDtoList = getBaseMapper().getPersonalAbility(AppUserUtil.getLoginUserId(), kpIds);
        setKpName(evalKpDtoList);
        return evalKpDtoList;
    }

    @Override
    public PersonalHistoryDto getPersonalHisScore(Integer year, Integer month) {
        Date startTime, endTime;
        if (Objects.isNull(year)) {
            year = DateUtil.thisYear();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        if (Objects.isNull(month)) {
            startTime = DateUtil.beginOfYear(calendar.getTime());
            endTime = DateUtil.endOfYear(calendar.getTime());
        } else {
            calendar.set(Calendar.MONTH, month - 1);
            startTime = DateUtil.beginOfMonth(calendar.getTime());
            endTime = DateUtil.endOfMonth(calendar.getTime());
        }
        //是否以月为维度统计（横坐标为日）
        boolean isMonth = !Objects.isNull(month);
        List<XAxisScoreDto> xAxisScoreDtos = getBaseMapper().getPersonalHisScore(AppUserUtil.getLoginUserId(), startTime, endTime, isMonth);
        //补充缺失的横坐标
        Map<String, XAxisScoreDto> studyScheduleDtoMap = xAxisScoreDtos.stream()
                .collect(Collectors.toMap(XAxisScoreDto::getXAxis, e -> e));
        xAxisScoreDtos = IntStream.rangeClosed(1, isMonth ? DateUtil.dayOfMonth(endTime) : 12).mapToObj(i -> {
            String xAxis = String.format("%02d", i);
            XAxisScoreDto xAxisScoreDto = studyScheduleDtoMap.get(xAxis);
            if (xAxisScoreDto == null) {
                xAxisScoreDto = new XAxisScoreDto();
                xAxisScoreDto.setScore(BigDecimal.ZERO);
            }
            xAxisScoreDto.setXAxis(xAxis + (isMonth ? "日" : "月"));
            return xAxisScoreDto;
        }).collect(Collectors.toList());
        PersonalHistoryDto personalHistoryDto = new PersonalHistoryDto();
        PersonalHistoryDto.Series series = new PersonalHistoryDto.Series();
        personalHistoryDto.getSeries().add(series);
        xAxisScoreDtos.forEach(e -> {
            personalHistoryDto.getXAxis().add(e.getXAxis());
            series.getData().add(e.getScore() == null ? "0.00" : e.getScore().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        });
        return personalHistoryDto;
    }

    @Override
    public List<TrainScheduleDto> trainSchedule(Integer year) {
        year = year == null ? DateUtil.thisYear() : year;
        List<TrainScheduleDto> trainScheduleDtos = getBaseMapper().trainSchedule(AppUserUtil.getLoginUserId(), year);
        if (!CollectionUtils.isEmpty(trainScheduleDtos)) {
            //获取知识点名称
            Set<String> kpIdSet = trainScheduleDtos.stream().map(e -> {
                String kpIds = e.getKpIds();
                return Arrays.stream(kpIds.split(",")).map(String::valueOf).collect(Collectors.toList());
            }).reduce(new HashSet<>(), (set, list) -> {
                set.addAll(list);
                return set;
            }, (s1, s2) -> {
                s1.addAll(s2);
                return s1;
            });
            if (!CollectionUtils.isEmpty(kpIdSet)) {
                Map<Long, String> knowledgePointIdMapByIds = manageBackendFeign.getKnowledgePointIdMapByIds(kpIdSet);
                trainScheduleDtos.forEach(e -> {
                    String kpNames = Arrays.stream(e.getKpIds().split(","))
                            .map(kpId -> knowledgePointIdMapByIds.get(Long.valueOf(kpId))).collect(Collectors.joining(","));
                    e.setKpNames(kpNames);
                });
            }
        }
        //补充缺失的月份
        Map<String, TrainScheduleDto> studyScheduleDtoMap = trainScheduleDtos.stream()
                .collect(Collectors.toMap(TrainScheduleDto::getMonth, e -> e));
        return IntStream.rangeClosed(1, 12).mapToObj(i -> {
            String month = String.format("%02d", i);
            TrainScheduleDto trainScheduleDto = studyScheduleDtoMap.get(month);
            if (trainScheduleDto == null) {
                trainScheduleDto = new TrainScheduleDto();
                trainScheduleDto.setKpNum(0);
            }
            trainScheduleDto.setMonth(month + "月");
            return trainScheduleDto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<EvalKpNewDto> getDeptKpScore(Long[] kpIds) {
        List<Long> userIdList = getLoginDepartUserIdList();
        List<EvalKpNewDto> evalKpDtoList = getBaseMapper().getDeptKpScore(userIdList, kpIds);
        setKpName(evalKpDtoList);
        return evalKpDtoList;
    }

    @Override
    public DeptAbilityDto deptAbilityBak(Long[] kpIds, Integer limit) {
        DeptAbilityDto deptAbilityDto = new DeptAbilityDto();
        Map<Long, String> loginDepartmentUserIdMap = getLoginDepartmentUserIdMap();
        limit = limit == null ? 3 : limit;
        List<ExamKpPersonAvgScore> scores = getBaseMapper().deptAbilityBak(loginDepartmentUserIdMap.keySet(), kpIds);
        if (!CollectionUtils.isEmpty(scores)) {
            List<DeptAbilityDto.Series> seriesList = IntStream.rangeClosed(1, limit).mapToObj(i -> {
                DeptAbilityDto.Series series = new DeptAbilityDto.Series();
                series.setName("第" + i + "名");
                return series;
            }).collect(Collectors.toList());
            deptAbilityDto.setSeries(seriesList);
            Map<String, List<DeptAbilityDto.SeriesData>> kpIdRankMap = scores.stream()
                    .collect(Collectors.groupingBy(ExamKpPersonAvgScore::getKpId, Collectors.mapping(score -> {
                        DeptAbilityDto.SeriesData seriesData = new DeptAbilityDto.SeriesData();
                        BigDecimal avgScore = score.getAvgScore();
                        seriesData.setValue(avgScore == null ? null : avgScore.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        seriesData.setName(loginDepartmentUserIdMap.get(score.getUserId()));
                        return seriesData;
                    }, Collectors.toList())));
            Map<Long, String> knowledgePointIdMapByIds = manageBackendFeign.getKnowledgePointIdMapByIds(kpIdRankMap.keySet());
            kpIdRankMap.forEach((kpId, rank) -> {
                deptAbilityDto.getXAxis().add(knowledgePointIdMapByIds.get(kpId));
                int kpIndex = deptAbilityDto.getXAxis().size() - 1;
                for (int i = 0; i < rank.size(); i++) {
                    if (i < seriesList.size()) {
                        List<DeptAbilityDto.SeriesData> seriesData = seriesList.get(i).getData();
                        while (seriesData.size() < kpIndex) {
                            seriesData.add(new DeptAbilityDto.SeriesData());
                        }
                        seriesData.add(rank.get(i));
                    }
                }
            });
        }
        return deptAbilityDto;
    }

    @Override
    public DeptAbilityNewDto deptAbility(Long[] kpIds, Integer limit) {
        DeptAbilityNewDto deptAbilityDto = new DeptAbilityNewDto();
        Map<Long, String> loginDepartmentUserIdMap = getLoginDepartmentUserIdMap();
        limit = limit == null ? 3 : limit;
        List<ExamKpPersonAvgScore> scores = getBaseMapper().deptAbilityBak(loginDepartmentUserIdMap.keySet(), kpIds);
        if (!CollectionUtils.isEmpty(scores)) {
            Map<String, List<EvalDeptNewDto>> kpIdRankMap = scores.stream()
                    .collect(Collectors.groupingBy(ExamKpPersonAvgScore::getKpId, Collectors.mapping(score -> {
                        EvalDeptNewDto evalDeptNewDto = new EvalDeptNewDto();
                        evalDeptNewDto.setKhNum(score.getQuestionCount());
                        evalDeptNewDto.setUserId(score.getUserId());
                        evalDeptNewDto.setUserName(loginDepartmentUserIdMap.get(score.getUserId()));
                        return evalDeptNewDto;
                    }, Collectors.toList())));
            Map<Long, String> knowledgePointIdMapByIds = manageBackendFeign.getKnowledgePointIdMapByIds(kpIdRankMap.keySet());
            kpIdRankMap.forEach((kpId, rank) -> {
                deptAbilityDto.getXAxis().add(knowledgePointIdMapByIds.get(kpId));
                deptAbilityDto.getSeries().add(rank);
            });
        }
        return deptAbilityDto;
    }

    @Override
    public List<EvalDeptNewDto> distribution(Long kpId) {
        Map<Long, String> loginDepartmentUserIdMap = getLoginDepartmentUserIdMap();
        List<EvalDeptNewDto> evalDeptNewDtos = getBaseMapper().distribution(loginDepartmentUserIdMap.keySet(), kpId);
        if (!CollectionUtils.isEmpty(evalDeptNewDtos)) {
            evalDeptNewDtos.forEach(e -> {
                e.setUserName(loginDepartmentUserIdMap.get(e.getUserId()));
            });
        }
        return evalDeptNewDtos;
    }

    @Override
    public EvalDto getPersonalScore() {
        EvalDto evalDto = new EvalDto();
        Long loginUserId = AppUserUtil.getLoginUserId();
        BigDecimal score = getBaseMapper().getPersonalScore(loginUserId);
        evalDto.setScore(score == null ? 0 : score.intValue());
        evalDto.caculateLevel();
        return evalDto;
    }

    @Override
    public EvalKpDto getDeptScore() {
        List<Long> loginDepartUserIdList = getLoginDepartUserIdList();
        BigDecimal score = getBaseMapper().getDeptScore(loginDepartUserIdList);
        EvalKpDto evalKpDto = new EvalKpDto();
        evalKpDto.setScore(score == null ? 0 : score.intValue());
        evalKpDto.caculateLevel();
        return evalKpDto;
    }

    @Override
    public List<EvalDeptNewDto> getPersonalDeptScore(Long kpId) {
        Map<Long, String> loginDepartmentUserIdMap = getLoginDepartmentUserIdMap();
        Set<Long> loginDepartUserIdList = loginDepartmentUserIdMap.keySet();
        List<EvalDeptNewDto> evalDeptDtoList = getBaseMapper().getPersonalDeptScore(loginDepartUserIdList, kpId);
        evalDeptDtoList.forEach(e -> e.setUserName(loginDepartmentUserIdMap.get(e.getUserId())));
        return evalDeptDtoList;
    }

    private List<Long> getLoginDepartUserIdList() {
        List<AppUser> appUsers = getLoginDepartmentAppUsers();
        List<Long> userIdList = appUsers.stream().map(AppUser::getId).collect(Collectors.toList());
        return userIdList;
    }

    private Map<Long, String> getLoginDepartmentUserIdMap() {
        List<AppUser> loginDepartmentAppUsers = getLoginDepartmentAppUsers();
        return loginDepartmentAppUsers.stream().collect(Collectors.toMap(AppUser::getId, AppUser::getNickname));
    }

    private List<AppUser> getLoginDepartmentAppUsers() {
        Long loginDepartmentId = AppUserUtil.getLoginDepartmentId();
        return sysDepartmentFeign.getDeptUserIds(loginDepartmentId);
    }

    private void setKpName(List<EvalKpNewDto> evalKpDtoList) {
        if (!CollectionUtils.isEmpty(evalKpDtoList)) {
            List<String> kpIdList = evalKpDtoList.stream().map(EvalKpNewDto::getKpId).collect(Collectors.toList());
            Map<Long, String> knowledgePointIdMapByIds = manageBackendFeign.getKnowledgePointIdMapByIds(kpIdList);
            evalKpDtoList.forEach(e -> e.setKpName(knowledgePointIdMapByIds.get(e.getKpId())));
        }
    }

}
