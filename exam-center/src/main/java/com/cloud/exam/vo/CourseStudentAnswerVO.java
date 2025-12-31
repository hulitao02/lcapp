package com.cloud.exam.vo;

import com.cloud.exam.model.exam.StudentAnswer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by dyl on 2021/04/08.
 * 缓存学员考试结果
 */
@ApiModel(value = "studentAnswerVO")
@Data
public class CourseStudentAnswerVO extends StudentAnswer {

    @ApiModelProperty(value = "试题内容")
    private String question;
    @ApiModelProperty(value = "试题选项")
    private String options;
    @ApiModelProperty(value = "试题答案")
    private String answer;
    @ApiModelProperty(value = "试题分数")
    private Double questionScore;
    @ApiModelProperty(value = "关键词")
    private String keywords;
    @ApiModelProperty(value = "试题解析")
    private String analysis;
    @ApiModelProperty(value = "是否正确标识")
    private Boolean flag = false;
    @ApiModelProperty(value = "考生答案")
    private Object stuAnswers;
    @ApiModelProperty(value = "考试总分数")
    private Double totalScore;
    @ApiModelProperty(value = "课程id")
    private Long courseId;
    private Double difficulty;
    // 收藏记录id
    private transient Long collectionId = null;
    // 文件地址
    private transient String fileAddr;
    // 影像缩略图地址
    private transient String localUrlPrefix;

}
