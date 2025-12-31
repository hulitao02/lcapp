package com.cloud.knowledge.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


@ApiModel("判读排序")
@Data
public class PdOrder implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String userName;
    // 标签类型1：直通式甲板2：常规战斗艇3：潜艇判读
    private Integer labelType;
    private Integer useCount;
    private String rowName;
    private String dataPropertyId;
    private String groupName;
    private int orderNum;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp updateTime;
    // 单位名称
    @TableField(exist = false)
    private String unitName;
    // 类型
    @TableField(exist = false)
    private String dataType;
    @TableField(exist = false)
    List<SysDictionary> sysDictionaries = new ArrayList<>();
    @TableField(exist = false)
    private String labelTypes;
}
