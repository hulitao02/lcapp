package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by dyl on 2021/06/17.
 */
@Data
public class CompetitionExamPaperRel implements Serializable {
    private static final long serialVersionUID = 5358025750160783323L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long acId;
    private Long paperId;
    private Integer paperType;
    private Integer isOvertime;
    private Integer paperSort;
    private Integer generateType;
}
