package com.cloud.model.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@ApiModel("属性的实体类Bean")
@Data
public class KnowledgePro implements Serializable {
    private static final long serialVersionUID = -998359721663823433L;


    @ApiModelProperty("数据库标识Id")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("知识点ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long kpId;

    @ApiModelProperty("属性的属性名称")
    private String proProname;

    @ApiModelProperty("属性Value")
    @TableField(exist = false)
    private String proProvalue;

    @ApiModelProperty("属性状态")
    private Integer status;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("属性的类型名称")
    private String proTypeName;

    @ApiModelProperty("属性知识点Id[字符串]")
    @TableField(exist = false)
    private String kpIdString;

    @ApiModelProperty("是否是属性结点")
    private int isProperty = 1;


    // 胡 新增 属性code，属性分组code，知识点code（均由第三方提供）
    private String proCode;
    private String proTypeCode;
    private String kpCode;

    /**
     * @return
     */
    public String getKpIdString() {
        if (Objects.nonNull(kpId)) {
            kpIdString = String.valueOf(kpId);
            return kpIdString;
        }
        return kpIdString;
    }

}
