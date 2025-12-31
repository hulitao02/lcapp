package com.cloud.knowledge.model;

import com.baomidou.mybatisplus.annotation.IdType;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class DjKp {
    private static final long serialVersionUID = -1118671419161945076L;

    @TableId(type = IdType.AUTO)
    private Long id;
    // 1：直通式甲板舰船2：常规战斗舰艇3：潜艇4：固定翼飞机5：旋翼飞机（直升机）6：装甲车辆
    private int type;
    private String kpId;
    private String kpName;
}
