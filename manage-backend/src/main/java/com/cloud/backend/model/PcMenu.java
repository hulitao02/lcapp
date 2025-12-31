package com.cloud.backend.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;


/**
 * @author:胡立涛
 * @description: TODO 产品端展示菜单
 * @date: 2022/3/16
 * @param:
 * @return:
 */
@Data
public class PcMenu implements Serializable {

    private static final long serialVersionUID = 749360940290141180L;

    private Long id;
    private Long parentId;
    private String menuName;
    private Integer level;
    private String url;
    private Integer sort;
    private Integer menuId;
    private String icon;
    private Timestamp createTime;
    private Timestamp updateTime;

    @TableField(exist = false)
    private String fileServer;
}
