package com.cloud.exam.model.eval;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;


@Data
@TableName("exam_eval_dept_his")
public class ExamEvalDeptHis implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private long kpId;
    private long evalScore;
    private Long departmentId;
    private Timestamp createTime;

}
