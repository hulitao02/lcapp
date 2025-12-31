package com.cloud.model.user;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

@Data
public class SysDepartment implements Serializable {
    private static final long serialVersionUID = 2347474956971846502L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private String dcode;
    private String dname;
    @TableField("parentid")
    private Long parentId;
    private String description;
    @TableField("createtime")
    private Date createTime;
    private Long creator;
    @TableField("updatetime")
    private Date updateTime;
    private Integer sort;
    @TableField(exist = false)
    private List<SysDepartment> child;
    @TableField(exist = false)
    private transient String creatorName;
    @TableField(exist = false)
    private Integer personCount;

    // 部门下的用户信息
    private transient List<AppUser> appUsers=new ArrayList<>();


}
