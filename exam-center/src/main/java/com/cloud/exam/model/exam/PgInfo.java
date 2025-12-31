package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;


@Data
public class PgInfo implements Serializable {
    private static final long serialVersionUID = -1118671419161945076L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long deptId;
    private String kpId;
    private String kpName;
    private String pdType;
    private Long examId;
    private Timestamp createTime;
    private String deptName;
    private String studentName;
    private String rightQuestion;
    private Integer totalQuestion;
    private String rightScore;
    private String totalScore;
    private String questionResult;
    private String scoreResult;
    private String result;
}
