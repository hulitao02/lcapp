package com.cloud.model.bean.vo.es;

import lombok.Data;

import java.util.List;


@Data
public class KnowLedgeSensesCageProperty {

    /**
     * 知识分类名称，比如说: 基本信息
     */
    private String cageName;

    private List<KnowLedgePros> pros;


}
