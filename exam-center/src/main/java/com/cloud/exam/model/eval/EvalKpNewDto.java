package com.cloud.exam.model.eval;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "评估传输对象")
public class EvalKpNewDto  implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "知识点ID")
	private String kpId;

	@ApiModelProperty(value = "知识点名称")
	private String kpName;

	@ApiModelProperty(value = "考核次数")
	private int khNum;
	private Double score;


}
