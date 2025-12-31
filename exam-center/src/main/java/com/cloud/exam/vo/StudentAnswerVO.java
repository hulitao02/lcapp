package com.cloud.exam.vo;

import com.cloud.exam.model.exam.StudentAnswer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by dyl on 2021/04/08.
 * 缓存学员考试结果
 */
@ApiModel(value = "studentAnswerVO")
@Data
public class StudentAnswerVO extends StudentAnswer {

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
    @ApiModelProperty(value = "考试时长")
    private String costTime;
    @ApiModelProperty(value = "考试总分数")
    private Double totalScore;
    @ApiModelProperty(value = "weboffice文件路径")
    private String modelUrl;
    @ApiModelProperty(value = "活动id")
    private Long examId;
    @ApiModelProperty(value = "考生添加试题标识")
    @JsonIgnoreProperties(ignoreUnknown = true)
    private Boolean mark = false;//默认false
    @ApiModelProperty(value = "考生座位号")
    private String placeNum;

    // 地址
    private transient String localUrlPrefix;
    // 文件服务器地址
    private transient String fileAddr;
    // 收藏记录id
    private transient Long collectionId;
    // 试题类型名称
    private transient String typeName;


}
