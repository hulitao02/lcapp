package com.cloud.model.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.sql.Timestamp;


@ApiModel("专家目标判读建议")
@Data
public class PdTips implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String userName;
    private Integer pdType;
    private String pdTypeName;
    private String filePath;
    private Integer checkUserId;
    private String checkUserName;
    private String checkDes;
    // 1：重要指标 2：非重要指标 3：无意义
    private Integer score;
    // 1：从单机版导入数据
    private Integer flg;
    // 判读要素
    private String pdYs;
    // 单机版专家判读意见id
    private String djId;
    private String pdContent;
    // 审核状态 0：待审核 1：审核通过 2：审核不通过
    private Integer checkState;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp updateTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp checkTime;

}
