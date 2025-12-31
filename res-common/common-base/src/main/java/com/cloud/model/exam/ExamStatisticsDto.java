package com.cloud.model.exam;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


@ApiModel("考试DTO")
@Data
public class ExamStatisticsDto {

    @ApiModelProperty("考试ID")
    private Integer examId;

    @ApiModelProperty("用户Id")
    private Integer userId;

    @ApiModelProperty("时间查询参数")
    private String queryDateYy;

    @ApiModelProperty("考试的类型")
    private List<Integer> examTypeList;

    @ApiModelProperty("考试的状态")
    private List<Integer> examStatusList;

    @ApiModelProperty("参考人员的状态")
    private List<Integer> userStatusList;

    @ApiModelProperty("试卷类型")
    private List<Integer> paperTypeList;

}
