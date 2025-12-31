package com.cloud.model.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class RelationKnowledgeData implements Serializable {

    private static final long serialVersionUID = -8732699715313323443L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String kpCode;
    private String knowledgeCode;
    private String proCode;
    private Long modelDataId;
    private String assemblyType;
    private Timestamp createTime;
}
