package com.cloud.model.bean.vo.es;

import lombok.Data;

@Data
public class KnowLedgePros {

    /**
     *
     * {component: "输入框", propertyValue: "知识1基础信息内容", title: "名称"
     *
     */
    private String title;

    private String propertyValue;

    private String component;


}
