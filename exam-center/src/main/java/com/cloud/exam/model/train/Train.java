package com.cloud.exam.model.train;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * Created by dyl on 2021/11/16.
 */
@Data
@ApiModel(value = "exam", description = "训练实体类")
public class Train {

    private Long id;
    private String name;
    private String describe;
    private Integer type;
    private Integer examStatus;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @ApiModelProperty(value = "活动开始时间")
    private Date startTime;
    @DateTimeFormat
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @ApiModelProperty(value = "活动结束时间")
    private Date endTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @ApiModelProperty(value = "活动创建时间")
    private Date updateTime;
    @DateTimeFormat
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @ApiModelProperty(value = "活动更新时间")
    private Date createTime;
    private Integer totalTime;
    private Long creator;

}
