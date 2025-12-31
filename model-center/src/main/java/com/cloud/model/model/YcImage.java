package com.cloud.model.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;


@ApiModel("图片")
@Data
public class YcImage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String filePath;
    private Integer type;
    private String content;
    @TableField(exist = false)
    private String fileServer;
}
