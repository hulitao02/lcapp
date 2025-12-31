package com.cloud.exam.model.course;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * Created by dyl on 2022/01/10.
 * 课程知识点关联实体类
 */
@Data
public class CourseUserRel {

    @TableId(type =IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long userId;
    private Integer status;
    private Double score;

    private transient String userName;
    private transient String courseTime;
    private transient String courseName;
}
