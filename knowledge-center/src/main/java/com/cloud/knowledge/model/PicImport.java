package com.cloud.knowledge.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class PicImport {
    private static final long serialVersionUID = -1118671419161945076L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private String parentKnowledge;
    private String kpName;
    private String picKnowledge;
    private String pswz;
    private String psjd;
    private String picPath;
    private byte[] picContent;
    private Timestamp createTime;
    // 图片知识的id 来源：图谱库
    private String knowledgeId;

    @TableField(exist = false)
    private String picPathtp;
    @TableField(exist = false)
    private String errorMessage = null;
}
