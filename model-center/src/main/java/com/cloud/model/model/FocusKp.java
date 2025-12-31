package com.cloud.model.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@ApiModel("知识点关注实体类")
@Data
public class FocusKp implements Serializable {

    private static final long serialVersionUID = -728253014164348504L;
    @TableId(type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("保存的用户ID")
    private Integer userId;

    @ApiModelProperty("关注的知识点ID")
    private String KpId;

    @ApiModelProperty("关注的知识点Name")
    private String kpName;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("状态值 1：有效，0：无效")
    private int status = 0 ;

}
