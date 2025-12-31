package com.cloud.exam.model.eval;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "评估传输对象")
public class EvalKpDto extends EvalDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value="知识点ID")
    private String kpId;

    @ApiModelProperty(value="知识点名称")
    private String kpName;
    
    @ApiModelProperty(value="考核次数")
    private int khNum;

	public String getKpId() {
		return kpId;
	}

	public void setKpId(String kpId) {
		this.kpId = kpId;
	}

	public String getKpName() {
		return kpName;
	}

	public void setKpName(String kpName) {
		this.kpName = kpName;
	}

	public int getKhNum() {
		return khNum;
	}

	public void setKhNum(int khNum) {
		this.khNum = khNum;
	}

}
