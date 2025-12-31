package com.cloud.model.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
@Data
@ApiModel("知识模板与知识点关联表")
public class ModelKp implements Serializable {

    private static final long serialVersionUID = -8466515346863572750L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("模板在知识点中的名称")
    private String name;

    @ApiModelProperty("知识点id")
    @JsonSerialize(using = ToStringSerializer.class)
    private String kpId;

    @ApiModelProperty("模板id")
    private Long modelId;

    @ApiModelProperty("导航方式：1知识链接")
    private Integer linkType;

    @ApiModelProperty("菜单图标地址")
    private String icoPath;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("状态(0:不可用，1:未对接数据，2:完成对接数据)")
    private Integer status;
}
