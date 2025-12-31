package com.cloud.model.bean.vo.es;

import lombok.Data;

import java.io.Serializable;

/**
 * ES中解析的 知识
 */
@Data
public class EsKnowlegdeJsonBean implements Serializable {

    private static final long serialVersionUID = 6087595627526882450L;


    private boolean success;
    private int code;
    private EsKnowledgeResult result;

}
