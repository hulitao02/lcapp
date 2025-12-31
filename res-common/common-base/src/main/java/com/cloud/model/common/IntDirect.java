package com.cloud.model.common;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author meidan
 */
@Data
@TableName("int_direct")
public class IntDirect implements Serializable {

    private static final long serialVersionUID = -4042376407588759033L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String country;
    private String describe;
}
