package com.cloud.exam.model.course;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by dyl on 2022/01/10.
 * 课程类
 */
@Data
public class CourseStudy implements Serializable{

    @TableId(type = IdType.AUTO)
    private Long id;
    @NotBlank(message = "课程名不能为空")
    @Length(max = 100,message = "名字长度最大100")
    private String name;
    @Length(max = 255,message = "说明长度最大200")
    private String describe;
    private Integer status;
    private Long teacherId;
    private Long creator;
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date startTime;
    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date endTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date createTime;
    private transient String courseTime;
    private transient String teacherName;

    //用户上课状态
    private transient Integer userStatus;
}
