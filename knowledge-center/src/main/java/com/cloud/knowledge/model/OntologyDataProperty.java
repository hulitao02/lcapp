package com.cloud.knowledge.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class OntologyDataProperty {
    private static final long serialVersionUID = -1118671419161945076L;

    private String id;
    private String value;
    private String name;
    private String superPropertyId;
    private String uri;
    private String code;
    private String dataType;
    private String dataUnit;
    // 删除标识 1：删除 其他情况均不为删除
    @TableField(exist = false)
    private Integer delFlg;

}
