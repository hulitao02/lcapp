package com.cloud.model.bean.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("舰载武器甲板信息")
public class RelationBean {
    private String dataType;
    private String id;
    private String label;
    private String name;
    private String value;
}
