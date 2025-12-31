package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

@Data
public class ExamDepartPaperRel implements Serializable {

    private static final long serialVersionUID = 5358025750160783323L;
    private Long acId;
    private Long departId;
    private Long paperId;
    @TableField(exist = false)
    private String paperName;
}
