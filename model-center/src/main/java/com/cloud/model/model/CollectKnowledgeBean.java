package com.cloud.model.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.model.bean.vo.es.KnowLedgeSensesCageProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 收藏知识点表
 */
@Data
@ApiModel("收藏知识Bean")
@TableName(value = "collect_knowledge")
public class CollectKnowledgeBean implements Serializable {

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
    @ApiModelProperty("修改人Id")
    private int updateUserId;
    @ApiModelProperty("收藏是否生效 1：已收藏，0:未收藏")
    private Integer collectStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("收藏时间")
    private Date collectDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("学习时间")
    private Date studyDate;

    @ApiModelProperty("是否学习 1：已学习，0:未学习")
    private Integer studyStatus;


    @ApiModelProperty("知识属性")
    @TableField(exist = false)
    private List<KnowLedgeSensesCageProperty> cagePropertyList;

    // 封面图
    @TableField(exist = false)
    private String photo;

    // 知识点id
    @TableField(exist = false)
    private String kpId;

    // 是否可以查看知识
    @TableField(exist = false)
    private boolean flag;

}
