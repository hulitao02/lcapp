package com.cloud.knowledge.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;


@ApiModel("国籍排序")
@Data
public class CountryOrder implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String countryName;
    private int orderNum;
    private int flg; // 1：舰船2：飞机
    @TableField(exist = false)
    private String countryNames;
}
