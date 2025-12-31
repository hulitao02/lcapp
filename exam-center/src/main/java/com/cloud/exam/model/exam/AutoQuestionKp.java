package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

@Data
public class AutoQuestionKp implements Serializable {
    private static final long serialVersionUID = -1118671419161945076L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private String relationCode;
    private String targetCode;
    private String proCode;
    private Long kpId;
}
