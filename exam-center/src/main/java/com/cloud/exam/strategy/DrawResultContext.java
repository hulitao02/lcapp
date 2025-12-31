package com.cloud.exam.strategy;

import com.cloud.exam.model.exam.ExamPlace;
import com.cloud.exam.model.exam.Paper;
import com.cloud.exam.vo.DrawResultVO;

import java.util.List;
import java.util.Map;

/**
 * Created by dyl on 2021/06/16.
 */
public class DrawResultContext {

    private DrawResultStrategy drawResultStrategy;

    public DrawResultContext(DrawResultStrategy drawResultStrategy) {
        this.drawResultStrategy = drawResultStrategy;
    }
    public List<DrawResultVO> executeStrategy(Long examId,Map<Long, List<Long>> map1, Map<Long, List<Long>> map2, List<String> list3, List<Paper> papers, List<ExamPlace> places){
        return drawResultStrategy.getDrawResult(examId,map1, map2, list3,papers,places);
    }
}
