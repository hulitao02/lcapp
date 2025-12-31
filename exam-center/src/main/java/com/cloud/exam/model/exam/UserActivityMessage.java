package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

/**
 * Created by dyl on 2021/04/20.
 */
@ApiModel(value="用户活动消息实体")
@Data
public class UserActivityMessage {
    @TableId(type = IdType.AUTO)
    private String id;
    private Long userId;
    private String message;
    private String isRead;
    private Date readTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;
    @TableField(exist = false)
    private boolean isNew = false;
}
