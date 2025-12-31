package com.cloud.exam.model.record;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 录屏信息
 */
@Data
@TableName(value = "record_info")
public class Record implements Serializable {


    @TableId(type= IdType.AUTO)
    private Long id;

    @ApiModelProperty("考生准考证号")
    private String identityCard;

    @ApiModelProperty("用户ID")
    private Long userId ;

    @ApiModelProperty("活动ID")
    private Long acId ;

    @ApiModelProperty("试卷Id")
    private Long paperId;

    @ApiModelProperty("录屏文件路径")
    private String recordPath;

    @ApiModelProperty("创建时间")
    private Date createTime;

//    @ApiModelProperty("提交人员信息")
//    private Long createUserId;



}
