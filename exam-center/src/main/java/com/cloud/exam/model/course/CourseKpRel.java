package com.cloud.exam.model.course;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * Created by dyl on 2022/01/10.
 * 课程知识点关联实体类
 */
@Data
public class CourseKpRel {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private String sensesId;
    private String bkClassLabel;
    private String sensesName;
    private Long kpId;
    private Integer sort;
}
