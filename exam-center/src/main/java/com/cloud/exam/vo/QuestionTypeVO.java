package com.cloud.exam.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by dyl on 2021/06/21.
 * 试卷题型
 */
@Data
public class QuestionTypeVO implements Serializable{

    //试题类型
    @JsonProperty("questionType")
    private Integer questionType;
    //试题难度(简单，一般，较难，复杂)
    @JsonProperty("difficultyType")
    private Double difficultyType;
    //试题分数和数量
    @JsonProperty("questionNum")
    private Integer questionNum;
    @JsonProperty("questionScore")
    private Double questionScore;



}
