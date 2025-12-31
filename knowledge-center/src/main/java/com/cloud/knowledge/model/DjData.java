package com.cloud.knowledge.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DjData {
    private static final long serialVersionUID = -1118671419161945076L;

    @TableId(type = IdType.AUTO)
    private Long id;
    // 1：直通式甲板舰船2：常规战斗舰艇3：潜艇4：固定翼飞机5：旋翼飞机（直升机）6：装甲车辆
    private int type;
    // 页面显示判读要素名称
    private String webMb;
    // 控件样式1：文本框2：下拉框3：图片列表
    private String wbType;
    // 图谱中判读要素名称
    private String tpMb;
    // 是否删除 1：删除0：未删除
    private int delFlg;
    // 在特征模块中的排列顺序
    private int orderNum;
    // 单位名称
    private String unitName;
    // 所在特征序号（如1：智能特征识别2：判读基础特征3：舰载武器）
    private int XuHao;
    @TableField(exist = false)
    private List<Map> dataList;
}
