package com.cloud.exam.strategy;

import com.cloud.exam.model.exam.ExamPlace;
import com.cloud.exam.model.exam.Paper;
import com.cloud.exam.vo.DrawResultVO;

import java.util.List;
import java.util.Map;

/**
 * Created by dyl on 2021/06/16.
 * 抽签策略
 */
public interface DrawResultStrategy {
    /**
     * @param examId  活动id
     * @param map1  单位-->试卷
     * @param map2  单位-->人员
     * @param list3 活动--> 场地座位信息
     * @param papers  单位试卷
     * @param places  单位场地
     * @return
     */
    public List<DrawResultVO> getDrawResult(Long examId,Map<Long, List<Long>> map1, Map<Long, List<Long>> map2, List<String> list3, List<Paper> papers, List<ExamPlace> places);
}
