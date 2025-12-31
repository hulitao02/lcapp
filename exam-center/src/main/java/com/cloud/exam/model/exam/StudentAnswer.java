package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author md
 */
@Data
public class StudentAnswer implements Serializable {

    private static final long serialVersionUID = -801406981257110175L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionId;
    private Long paperId;
    private String stuAnswer;
    private Double actualScore;
    private Long studentId;
    private Date createTime;
    private Date updateTime;
    private Integer paperType;
    private String judgeRemark;
    //试题题型
    private Integer type;
    private Integer intType;
    private String placeNum;
    private Long acId;
    private String pdType;

    @ApiModelProperty(value = "试题对应知识点信息")
    @TableField(exist = false)
    private List<HashMap<String, Object>> kpDetails;
    private String kpScores;
}
