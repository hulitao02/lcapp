package com.cloud.exam.vo;

import com.cloud.model.common.KnowledgePoints;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by dyl on 2021/08/17.
 */
@Data
public class CheckExamVO {

    private Long examId;
    private String examName;
    private List<KnowledgePoints> kList;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date examDate;
    private Integer examTime;
    private double score;

}
