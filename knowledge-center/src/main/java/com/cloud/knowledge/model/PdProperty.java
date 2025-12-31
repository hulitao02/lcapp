package com.cloud.knowledge.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PdProperty {
    private static final long serialVersionUID = -1118671419161945076L;

    @TableId(type = IdType.AUTO)
    private Long id;
    // 1：直通式甲板舰船2：常规战斗舰艇3：潜艇4：固定翼飞机5：旋翼飞机（直升机）6：装甲车辆
    private int labelType;
    // 类型
    private String dataType;
    private String dataPropertyId;
    private String propertyName;
    private String groupName;
    // 在分组中的排列顺序
    private int orderNum;
    // 单位名称
    private String unitName;
    private String typeId;
    // 枚举类型值存取字段
    @TableField(exist = false)
    List<SysDictionary> sysDictionaries=new ArrayList<>();
}
