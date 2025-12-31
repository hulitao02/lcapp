package com.cloud.exam.vo;

import com.cloud.exam.model.exam.CompetitionExamPaperRel;
import lombok.Data;

/**
 * Created by dyl on 2021/06/17.
 */
@Data
public class CompetitionExamPaperRelVO extends CompetitionExamPaperRel {

    //试卷名称
    private String paperName;
    //试题数量
    private Integer paperQuestionNum;
    //将要操作试题的下标
    private Integer questionIndex;
    //试卷总分
    private Double paperScore;
}
