package com.cloud.model.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ModelData implements Serializable {

    private static final long serialVersionUID = -8732699715313323443L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("模板与知识点关联id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long modelKpId;

    @ApiModelProperty("知识点ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private String kpId;

    @ApiModelProperty("组件名称")
    private String assemblyName;

    @ApiModelProperty("属性ID,外键")
    private String proId;

    @ApiModelProperty("属性的属性名称")
    private String proProname;

    @ApiModelProperty("属性分组")
    private String proTypeName;

    @ApiModelProperty("约束条件")
    private String constraintForce;

    @ApiModelProperty("查询语句")
    private String sqlStr;

    @ApiModelProperty("状态(1:可用，0:不可用)")
    private Integer status;

    private String assemblyType;

    // 是否为知识关系数据标识
    @TableField(exist = false)
    private String flg;


}
