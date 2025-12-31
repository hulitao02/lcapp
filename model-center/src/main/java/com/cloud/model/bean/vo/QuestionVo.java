package com.cloud.model.bean.vo;

import com.cloud.model.model.Question;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel("返回前端展示的VO")
@Data
public class QuestionVo extends Question {


    @ApiModelProperty("针对一个问题的回复总数")
    private Integer replyCount;


}
