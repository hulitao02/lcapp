package com.cloud.model.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

@Data
public class ModelGroup implements Serializable {

    private static final long serialVersionUID = -6744641006845531646L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /**
     * 状态(1:可用，0:不可用)
     */
    private Integer status;
}
