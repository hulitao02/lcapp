package com.cloud.backend.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;


/**
 * @author: 胡立涛
 * @description: TODO 智能推荐参数设置
 * @date: 2022/5/30
 * @param:
 * @return:
 */
@Data
public class KnowledgeParam implements Serializable {

    private static final long serialVersionUID = 749360940290141180L;

    private Long id;
    private String paramName;
    private String paramValue;
    private String des;
    private Timestamp createTime;
    private Timestamp updateTime;
    @TableField(exist = false)
    private String fileServer;
}
