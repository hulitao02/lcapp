package com.cloud.model.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;


@Data
public class KnowledgePic implements Serializable {

    private static final long serialVersionUID = -8732699715313323443L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("知识点ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private String kpId;

    @ApiModelProperty("组件名称")
    private String assemblyName;

    @ApiModelProperty("属性ID,外键")
    private String proId;

    @ApiModelProperty("属性的code")
    private String proProname;

    @ApiModelProperty("属性分组code")
    private String proTypeName;

    private String assemblyType;
    // 模板与知识点关系id
    private Long modelKpId;

}
