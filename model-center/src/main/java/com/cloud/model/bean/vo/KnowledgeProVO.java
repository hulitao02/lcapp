package com.cloud.model.bean.vo;

import com.cloud.model.model.KnowledgePro;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;


@Data
@ApiModel("知识点和属性关联VO")
public class KnowledgeProVO extends KnowledgePro {
    private List<KnowledgeProVO> childrenList;

}
