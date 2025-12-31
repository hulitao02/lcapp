package com.cloud.exam.model.eval;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;


@Data
@TableName("exam_eval_person")
public class ExamEvalPerson implements Serializable {

    private static final long serialVersionUID = 1L;

    private long acId;
    private long kpId;
    private long evalScore;
    private long userId;
    private Integer month;
    private Long departmentId;
    private Timestamp createTime;

}
