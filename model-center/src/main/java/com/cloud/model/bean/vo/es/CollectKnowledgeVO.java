package com.cloud.model.bean.vo.es;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 收藏知识点表
 */
@Data
@ApiModel("前端显示收藏知识VO")
public class CollectKnowledgeVO {

    @ApiModelProperty("数据库标识")
    private int id;

    @ApiModelProperty("用户ID")
    private int user_id;

    @ApiModelProperty("知识Code")
    private String knowledge_code;

    @ApiModelProperty("知识名称")
    private String knowledge_name;

    @ApiModelProperty("创建时间")
    private Date create_time;

    @ApiModelProperty("更新时间")
    private Date update_time;

    @ApiModelProperty("修改人Id")
    private int update_user_id;


}
