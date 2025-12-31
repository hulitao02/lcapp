package com.cloud.model.model;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


@Data
@ApiModel("学习计划关联的知识")
public class StudyKnowledge implements Serializable {

    private static final long serialVersionUID = -2058966622408895025L;

    @ApiModelProperty("数据库标识ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("学习计划Id")
    private Integer planId;

    @ApiModelProperty("学习计划关联的知识")
    private String knowledgeId;

    @ApiModelProperty("学习计划关联的知识名称")
    private String knowledgeName;

    @ApiModelProperty("学习计划关联的知识点Id")
    private String kpId;

    @ApiModelProperty("计划创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty("状态，1: 有效")
    private Integer status;

    /**
     * 是否已学习，属性值 。
     */
    @ApiModelProperty("是否已学习 2：已学习")
    private Integer studyStatus;

    // 是否可以查看知识
    @TableField(exist = false)
    private boolean flag;
}
