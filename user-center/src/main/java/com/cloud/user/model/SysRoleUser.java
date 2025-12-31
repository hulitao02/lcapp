package com.cloud.user.model;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;


@Data
public class SysRoleUser implements Serializable {

    private static final long serialVersionUID = 749360940290141180L;

    private Integer userId;// 用户id
    private Integer roleId;// 角色id



}
