package com.cloud.exam.model.course;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * Created by dyl on 2022/01/10.
 * 课程试题关联实体类
 */
@Data
public class CourseQuestionRel {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long QuestionId;
    private Integer type;
    private Integer sort;
    private Double score;
}
