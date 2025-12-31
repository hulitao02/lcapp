package com.cloud.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;


@Data
public class LcTaskUser implements Serializable {

    private static final long serialVersionUID = 749360940290141180L;

    @TableId(type= IdType.AUTO)
    private Integer id;
    private int taskId;// 任务id
    private int userId;// 责任人id
    private int deptId;// 部门id
    private int flg;// 是否为创建人 1：是 0：否

}
