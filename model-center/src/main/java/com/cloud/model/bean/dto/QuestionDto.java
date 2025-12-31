package com.cloud.model.bean.dto;

import com.cloud.model.model.Question;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("问题前端传递的参数DTO")
@Data
public class QuestionDto extends Question {


    private Integer pageNum;
    private Integer pageSize;
    /**
     * 回答的总量
     */
    @ApiModelProperty("问题回复总数")
    private Integer replyCount;

    @ApiModelProperty("查询条件")
    private String queryParams;


    // 胡 用户名称，部门名称
    private String userName;
    private String departName;


}
