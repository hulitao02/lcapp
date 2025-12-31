package com.cloud.model.bean.vo;

import com.cloud.model.model.ModelData;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ModelDataVO extends ModelData implements Serializable {


    @ApiModelProperty("知识点名称")
    private String kpName;
    @ApiModelProperty("关注Id")
    private Integer focusId;
    @ApiModelProperty("状态(1:关注，0:不可用)")
    private Integer focusStatus;
    @ApiModelProperty("用户Id")
    private Integer userId;
    @ApiModelProperty("真实知识的值")
    private String proProvalue;
    @ApiModelProperty("模板名称 前端使用")
    private String servicePath;

    @ApiModelProperty("模板名称 前端使用")
    private String modelName;
    // 是否收藏 1：收藏 非1：未收藏
    private Integer collectStatus;

    private String picPath;

    private String proCode;
    private String proTypeCode;
    // 复杂模板： 区分子项中的资源和属性 1：属性 0：资源
    private String constraintForce;
    // 复杂模板： 存储子项id（model_control表的id值）
    private String sqlStr;

}
