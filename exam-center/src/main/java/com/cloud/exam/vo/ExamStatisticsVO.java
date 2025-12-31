package com.cloud.exam.vo;

import com.cloud.exam.model.exam.Exam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel("考试结果返回DTO")
@Data
public class ExamStatisticsVO extends Exam {

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("用户状态")
    private Integer userStatus;


}
