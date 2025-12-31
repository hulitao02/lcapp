package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by dyl on 2021/07/02.
 * 竞答试卷的试卷对应的每级难度试题分值
 */

@TableName(value = "competition_paper_question_score")
@Data
public class CompetitionPaperQuestionScore  implements Serializable{

    private static final long serialVersionUID = 5358025750160783323L;
    @TableId(type=IdType.AUTO)
    private Long id;
    private Long paperId;
    private Double difficultyLevel1;
    private Double difficultyLevel2;
    private Double difficultyLevel3;
    private Double difficultyLevel4;
    private Double difficultyLevel5;
    private Integer questionTime;
}
