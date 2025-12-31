package com.cloud.knowledge.model;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class XxClass implements Serializable {
    private String classId;
    private String score;
    private Long userId;
    private Timestamp createTime;
    private Timestamp updateTime;
}
