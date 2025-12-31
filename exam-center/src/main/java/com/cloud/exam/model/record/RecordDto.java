package com.cloud.exam.model.record;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class RecordDto extends Record {


    @ApiModelProperty("用户名称")
    private String userName;

    @ApiModelProperty("用户单位")
    private String examDepartName;

    @ApiModelProperty("考试名称")
    private String examName;


    @ApiModelProperty("试卷名称")
    private String paperName;


    @ApiModelProperty("考试类型名称")
    private String examTypeName;


    @ApiModelProperty("考试得分")
    private Long examScore;


}
