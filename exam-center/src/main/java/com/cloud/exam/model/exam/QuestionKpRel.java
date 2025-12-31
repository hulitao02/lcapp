package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by dyl on 2021/10/26.
 * 试题知识点关联实体类
 */
@Data
public class QuestionKpRel implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionId;
    private String kpId;


}
