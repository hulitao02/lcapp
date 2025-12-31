package com.cloud.knowledge.model;

import lombok.Data;

@Data
public class SysDictionary {
    private static final long serialVersionUID = -1118671419161945076L;

    private String code;
    private String name;
    private String description;
    private String typeId;
}
