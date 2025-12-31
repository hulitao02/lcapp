package com.cloud.exam.vo;

import lombok.Data;

/**
 * Created by dyl on 2021/05/17.
 */
@Data
public class ExamVO {
    private Long id;
    private String examName;
    private String describe;
    private String examDate;
    private String paperName;
    private Integer type;
    private Integer isSign;
    private Integer isFix;
    private Long scorechartsId;
    private Double initScore;
    private Integer screenRecord;
}
