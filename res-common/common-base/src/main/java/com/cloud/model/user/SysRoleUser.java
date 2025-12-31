package com.cloud.model.user;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;


@Data
public class SysRoleUser {
    private Long userid;
    private Long roleid;

}
