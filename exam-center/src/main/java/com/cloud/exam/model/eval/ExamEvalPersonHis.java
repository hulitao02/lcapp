package com.cloud.exam.model.eval;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;


@Data
@TableName("exam_eval_person_his")
public class ExamEvalPersonHis implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private long id;
    private long kpId;
    private long evalScore;
    private long userId;
    private Integer month;
    private Long departmentId;
    private Timestamp createTime;
    private Integer curFlg;
    private Timestamp updateTime;
    private Long khNum;
    private Long xlNum;
    private Long totalNum;
    private long acId;

}
