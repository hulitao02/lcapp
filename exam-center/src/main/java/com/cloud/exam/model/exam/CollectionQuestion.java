package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @author:胡立涛
 * @description: TODO 试题收藏表
 * @date: 2022/8/19
 * @param:
 * @return:
 */
@Data
public class CollectionQuestion {
    private static final long serialVersionUID = -1118671419161945076L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long questionId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createTime;
    private String question;
    private String type;
    private Integer questionFlg;
}
