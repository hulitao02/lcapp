package com.cloud.exam.model.exam;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExamDepartUserRel implements Serializable {

    private static final long serialVersionUID = 5358025750160783323L;
    private Long acId;
    private Long departId;
    private Long memberId;
}
