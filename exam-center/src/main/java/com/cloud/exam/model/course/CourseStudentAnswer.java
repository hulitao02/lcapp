package com.cloud.exam.model.course;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author md
 */
@Data
public class CourseStudentAnswer implements Serializable {

    private static final long serialVersionUID = -801406981257110175L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionId;
    private Long courseId;
    private String stuAnswer;
    private Double actualScore;
    private Long studentId;
    private Date createTime;
    private Date updateTime;
    private Integer paperType;
    private String judgeRemark;
    //试题题型
    private Integer type;

}
