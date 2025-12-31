package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import javassist.bytecode.ByteArray;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;


@Data
public class PicImport implements Serializable {
    private static final long serialVersionUID = -1118671419161945076L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private String parentKnowledge;
    private String picKnowledge;
    private String pswz;
    private String psjd;
    private String picPath;
    private String picContent;
    private Timestamp createTime;
}
