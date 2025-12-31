package com.cloud.model.common;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Red
 */
@Data
@TableName("knowledge_points")
public class KnowledgePoints implements Serializable {
    private static final long serialVersionUID = -8626729177327254025L;
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("pointname")
    private String pointName;
    @TableField("parentid")
    private Long parentId;
    private String describe;
    @TableField("createtime")
    private Date createTime;
    @TableField("updatetime")
    private Date updateTime;
    private Long creator;
    private Integer level;
    private Integer status;

    // 胡 新增 知识点code，知识点父节点code
    private String code;
    private String parentCode;
    private String kpLabel;
    @TableField(exist = false)
    private List<KnowledgePoints> child;


}
