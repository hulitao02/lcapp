package com.cloud.model.bean.vo;


import com.cloud.model.model.StudyPlan;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel("学习计划相关知识VO类")
public class PlanKnowledgeVO extends StudyPlan {

    @ApiModelProperty("学习计划关联的知识")
    private String knowledgeId;
    @ApiModelProperty("学习计划关联的知识名称")
    private String knowledgeName;
    @ApiModelProperty("学习计划关联的知识点Id")
    private String kpId;
    @ApiModelProperty("是否已学习")
    private  Integer studyStatus;

    @ApiModelProperty("学习计划关联的知识，数据库标识ID")
    private Integer planKnId;





}
