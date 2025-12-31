package com.cloud.knowledge.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class OntologyIndividualDp {
    private static final long serialVersionUID = -1118671419161945076L;

    @TableId(type = IdType.AUTO)
    private String id;
    private String dataPropertyValue;
}
