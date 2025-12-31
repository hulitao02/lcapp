package com.cloud.model.bean.vo.es;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * ES 中result 结构
 */
@Data
@ApiModel("ES知识，返回结果")
public class EsKnowledgeResult {

    @ApiModelProperty("当前页码")
    private int current;
    @ApiModelProperty("一共多少页")
    private int pages;
    @ApiModelProperty("当前页的记录")
    private int size;
    private int total;

    @ApiModelProperty("知识点名称")
    private String bkClassLabel;
    @ApiModelProperty("知识点ID")
    private String classId;

    @ApiModelProperty("查询知识列表时，知识结合")
    private List<EsKnowledgeRecord> records;
    /**
     * 单个知识，封装结果
     */
    @ApiModelProperty("知识ID")
    private String sensesId;
    @ApiModelProperty("知识名称")
    private String sensesName;
    //    知识点下的属性
    @ApiModelProperty("通过ID查询知识时，当前知识的所有的属性集合")
    private List<KnowLedgeSensesCageProperty> sensesCageProperty;

}
