package com.cloud.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.sql.Timestamp;



@Data
public class LcTask implements Serializable {

    private static final long serialVersionUID = 749360940290141180L;

    @TableId(type= IdType.AUTO)
    private Integer id;

    private String name;// 任务名称
    private int type;// 工作类别
    private String content;// 任务内容
    private int state;// 任务状态
    private String wcsx;//完成时限
    private int jjcd;//紧急程度
    private  String wcjd;// 完成进度
    private String yxj;// 优先级别
    private String planStartTime;//计划开始时间
    private String planEndTime;//计划结束时间
    private String relStartTime;// 实际开始时间
    private String relEndTime;// 实际结束时间
    private int createUser;// 创建用户id
    private int deptId;//创建人所在部门id
    private int flg;//是否按期完成任务 1：是 0：否


    @DateTimeFormat
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Timestamp createTime;// 创建时间
    @DateTimeFormat
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Timestamp updateTime;// 更新时间


    private transient String userIds;// 责任人ids
    private transient String userNames;// 责任人names
    private transient String typeName;// 工作类别名称
    private transient String userDeptNames;// 责任人部门names


}
