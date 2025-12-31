package com.cloud.model.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@ApiModel("问题实体类")
@Data
public class Question implements Serializable {
    private static final long serialVersionUID = 718121362778150220L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    //  用户Id
    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("知识点Id")
    @JsonSerialize(using = ToStringSerializer.class)
    private String kpId;
    @ApiModelProperty("知识Id")// 知识ID
    @JsonSerialize(using = ToStringSerializer.class)
    private String knId;
    @ApiModelProperty("模板和知识点关联Id")
    private Integer modelKpId;
    @ApiModelProperty("问题标题")
    private String questionTitle;
    @ApiModelProperty("问题内容")
    private String content;
    @ApiModelProperty("可能存在的 页面的元素信息")
    private String contentTagJson;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    private Integer status;
    @ApiModelProperty("是否已回答，1:回答")
    private Integer isAnswer;
    @ApiModelProperty("问题类型")
    private Integer type;


    @ApiModelProperty("知识名称")
    private String knName;
    @ApiModelProperty("知识点名称")
    private String kpName;

}
