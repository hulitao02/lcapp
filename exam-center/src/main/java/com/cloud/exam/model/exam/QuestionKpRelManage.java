package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

@Data
public class QuestionKpRelManage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionId;
    private String kpId;


}
