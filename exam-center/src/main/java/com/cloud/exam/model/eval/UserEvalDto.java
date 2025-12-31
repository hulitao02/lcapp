package com.cloud.exam.model.eval;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "考试活动个人评估传输对象")
public class UserEvalDto extends EvalDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(value="用户ID")
    private long userId;
    
    @ApiModelProperty(value="用户名")
    private String userName;
	
	@ApiModelProperty(value="试卷分数")
	private double examScore;

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

	public double getExamScore() {
		return examScore;
	}

	public void setExamScore(double examScore) {
		this.examScore = examScore;
	}

}
