package com.cloud.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;


@Data
public class LcTaskState implements Serializable {

    private static final long serialVersionUID = 749360940290141180L;

    @TableId(type= IdType.AUTO)
    private Integer id;
    private int taskId;// 任务id
    private int createUser;// 创建用户id
    private int state;// 任务状态
    private Timestamp createTime;// 创建时间

    private transient String createUserName;// 创建用户名
}
