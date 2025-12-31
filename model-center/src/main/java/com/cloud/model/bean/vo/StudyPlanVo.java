package com.cloud.model.bean.vo;


import com.cloud.model.model.StudyPlan;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 学习计划VO
 */
@ApiModel("学习计划VO类")
@Data
public class StudyPlanVo extends StudyPlan {

    @ApiModelProperty("知识ID")
    private String knowledgeId;

    @ApiModelProperty("知识名称")
    private String knowledgeName;

    @ApiModelProperty("是否学习，2: 表示已学习")
    private Integer studyStatus;

    @ApiModelProperty("学习时间")
    private Date studyDate;

    @ApiModelProperty("已完成的学习")
    private List<StudyPlanVo> studyedList;

    @ApiModelProperty("未完成的学习")
    private List<StudyPlanVo> unstudyedList;

    @ApiModelProperty("已完成的学习")
    private Integer studyedCount;

    @ApiModelProperty("未完成的学习")
    private Integer unstudyedCount;

    @ApiModelProperty("学习计划关联的知识点Id")
    private String kpId;


}
