package com.cloud.model.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;


@ApiModel("舰面符号")
@Data
public class Image implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String filePath;
    @TableField(exist = false)
    private String fileServer;
}
