package com.cloud.exam.model.exam;


import lombok.Data;

import java.io.Serializable;

/**
 * @author md
 */
@Data
public class ExamStudentRel implements Serializable {

    private static final long serialVersionUID = 4152736535018724917L;
    private Long id;
    private Long examId;
    private Long paperId;
    private Long studentId;
    private Integer setNum;
    private Integer isJudge;
    private Integer getScore;
    private Long judge;
}
