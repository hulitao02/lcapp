package com.cloud.knowledge.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;


@ApiModel("知识点")
@Data
public class OntologyClass implements Serializable {

    private String id;
    private String pointName;
    private String parentId;
}
