package com.cloud.exam.model.eval;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "评估传输对象")
public class EvalDeptDto extends EvalDto implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value="用户ID")
    private long userId;
    
    @ApiModelProperty(value="用户名")
    private String userName;
    
    @ApiModelProperty(value="考核次数")
    private int khNum;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getKhNum() {
		return khNum;
	}

	public void setKhNum(int khNum) {
		this.khNum = khNum;
	}

}
