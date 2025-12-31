package com.cloud.model.common;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("backend_dict")
public class Dict implements Serializable {
    private static final long serialVersionUID = 749360940290141181L;
    @TableId(type = IdType.AUTO)
    private long id;
    @TableField("dictname")
    private String dictName;
    @TableField("dictvalue")
    private String dictValue;
    @TableField("dicttype")
    private String dictType;
    @TableField("dictdescription")
    private String dictDescription;
}
