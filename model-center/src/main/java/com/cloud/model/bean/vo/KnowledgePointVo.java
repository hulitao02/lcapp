package com.cloud.model.bean.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("知识点")
public class KnowledgePointVo implements Serializable {

    private static final long serialVersionUID = 2149631822718157475L;

    @ApiModelProperty("知识点id")
    @JsonSerialize(using = ToStringSerializer.class)
    private String kpId;

    @ApiModelProperty("知识点名称")
    private String kpName;

    @ApiModelProperty("知识点父级id")
    @JsonSerialize(using = ToStringSerializer.class)
    private String kpParentId;

    @ApiModelProperty("知识点挂载模板数量")
    private Integer modelCount;

    @ApiModelProperty("知识点挂载已绑定数据的模板数量")
    private Integer modelDataCount;

    private List<KnowledgePointVo> childList;
}
