package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;


/**
 * 关联表
 * @author md
 */
@Data
public class PaperManageRel implements Serializable {

    private static final long serialVersionUID = 559062395041526194L;
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    private Long paperId;

    private Long questionId;

    private Double score;

    private String scoreBasis;

    private Integer sort;

    private Integer questionTime;
}
