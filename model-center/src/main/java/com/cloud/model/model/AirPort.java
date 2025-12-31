package com.cloud.model.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.sql.Timestamp;


@ApiModel("机场模板")
@Data
public class AirPort implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String sourceId;
    private String targetId;
    // 设施对应的知识点
    private Long kpId;
    // 知识点与模板对接的知识点
    private Long orginKid;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createTime;

}
