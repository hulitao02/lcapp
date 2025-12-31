package com.cloud.exam.model.exam;


import lombok.Data;

import java.io.Serializable;

@Data
public class ExamManageGroupRel implements Serializable {

    private static final long serialVersionUID = 6580438454576715378L;
    private Long acId;
    private Long managegroupId;
    private Long memberId;
    //private Integer master;
}
