package com.cloud.exam.model.exam;

import lombok.Data;

/**
 * Created by dyl on 2021/08/03.
 * 理论考试客观题自动评分信息
 */
@Data
public class StudentAnswerDetails {
    private Integer questionType;
    private Integer totalNumber = 0;
    private Integer correctNumber = 0;
    private Integer wrongNumber = 0;
    private double questionScore;
    private double totalScore = 0.0;
}
