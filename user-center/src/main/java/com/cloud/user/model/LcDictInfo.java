package com.cloud.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;


@Data
public class LcDictInfo implements Serializable {

    private static final long serialVersionUID = 749360940290141180L;

    @TableId(type= IdType.AUTO)
    private Integer id;
    private String name;// 字典名称
    private Integer classId;// 分类id
}
