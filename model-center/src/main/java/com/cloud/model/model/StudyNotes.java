package com.cloud.model.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@ApiModel("学习笔记")
@Data
public class StudyNotes implements Serializable {
    private static final long serialVersionUID = -2301635164718524147L;

    @ApiModelProperty("学习笔记数据库Id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("学习笔记数据库Id")
    private Integer userId;
    private String notesInfo;

    @ApiModelProperty("知识Id")// 知识ID
    @JsonSerialize(using = ToStringSerializer.class)
    // 胡 知识id
    private String knId;
    @ApiModelProperty("知识点Id")// 知识点ID
    @JsonSerialize(using = ToStringSerializer.class)
    private String kpId;

    private String knName;
    private String kpName;

    @ApiModelProperty("模版和知识点关联Id")
    private Integer modelKpId;
    @ApiModelProperty("组件名称")
    private String assemblyName;

    // 图片路径，多个图片用逗号进行分割
    private String picPaths;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    private Integer status;

//    /**
//     * 查询使用
//     */
//    @ApiModelProperty("知识名称byte")
//    private BLOB knNameByte;
//
//    @ApiModelProperty("知识点名称byte")
//    private BLOB kpNameByte;
//
//    @ApiModelProperty("笔记内容byte数组")
//    private BLOB notesInfoByte;


}
