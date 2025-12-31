package com.cloud.model.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * @author:胡立涛
 * @description: TODO 知识浏览
 * @date: 2022/1/12
 * @param:
 * @return:
 */
@Data
@ApiModel("知识浏览")
@TableName(value = "knowledge_view")
public class KnowledgeViewBean implements Serializable {

    private static final long serialVersionUID = 358479206615678587L;

    @ApiModelProperty("数据库标识ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @ApiModelProperty("用户ID")
    private int userId;

    // ES中对应
    @ApiModelProperty("知识Id")
    private String sensesId;
    @ApiModelProperty("知识名称")
    private String sensesName;


    @ApiModelProperty("知识点ID ")
    private String classId;
    @ApiModelProperty("知识点名称 ")
    private String bkClassLabel;


    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("创建时间")
    private Date createTime;
    @ApiModelProperty("更新时间")

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("学习时间")
    private Date studyDate;

}
