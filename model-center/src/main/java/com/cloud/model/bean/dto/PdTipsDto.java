package com.cloud.model.bean.dto;

import com.cloud.model.model.Question;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel("专家判读意见dto")
@Data
public class PdTipsDto extends Question {

    // 判读要素
    private String pdYs;
    // 专家id
    private Integer userId;
    // 专家名称
    private String userName;
    // 判读类型id 1：直通式甲板舰船判读2：常规战斗舰艇3：潜艇4：固定翼飞机5：旋翼机（直升机）6：装甲车辆
    private Integer pdType;
    // 判读类型名称
    private String pdTypeName;
    // 1：重要指标 2：非重要指标 3：无意义
    private Integer score;
    // 多张图片之间用逗号进行分割
    private String filePath;
    // 审核状态 0：待审核 1：审核通过 2：审核不通过
    private Integer checkState;
    // 专家建议
    private String pdContent;
    // 单机版专家判读意见id（根据该id和专家id可以确定一条信息）
    private String djId;
}
