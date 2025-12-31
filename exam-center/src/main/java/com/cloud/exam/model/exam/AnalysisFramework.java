package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by dyl on 2022/5/24.
 * 情报分析模板
 */
@Data
public class AnalysisFramework implements Serializable{

    @TableId(type = IdType.AUTO)
    private Long id ;
    private Integer type ;
    private String name ;
    private String detail ;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}
