package com.cloud.model.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@ApiModel("学习计划实体")
@TableName(value = "study_plan")
public class StudyPlan implements Serializable {
    private static final long serialVersionUID = -4805381205433687224L;

    @ApiModelProperty("数据库标识ID")
    @TableId(value = "id", type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Integer id;

    @ApiModelProperty("用户Id")
    private Integer userId;

    @ApiModelProperty("计划内容")
    private String planContent;

    @ApiModelProperty("计划总结")
    private String summary;

    @ApiModelProperty("计划结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date planStartTime;

    @ApiModelProperty("计划创建时间 开时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty("计划更新时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty("计划状态，1:有效")
    private Integer status;

    @ApiModelProperty("关联的知识,不映射实体, 实体里 studyStatus ，真实是否已学习 ")
    @TableField(exist = false)
    private List<StudyKnowledge> studyKnowledgeList;

    @ApiModelProperty("是否可以修改")
    @TableField(exist = false)
    private boolean update;


    public String getFormatTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
        if(Objects.nonNull(planStartTime)){
            return dateFormat.format(planStartTime);
        }else{
            return "";
        }
    }



}
