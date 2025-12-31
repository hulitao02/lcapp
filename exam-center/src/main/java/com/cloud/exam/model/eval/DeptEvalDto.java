package com.cloud.exam.model.eval;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "考试活动团体评估传输对象")
public class DeptEvalDto extends EvalDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(value="单位ID")
	private Long deptId;
	
	@ApiModelProperty(value="单位名称")
	private String deptName;
	
	@ApiModelProperty(value="考试人数")
	private int num;
	
	@ApiModelProperty(value="总成绩")
	private double totalScore;

	public Long getDeptId() {
		return deptId;
	}

	public void setDeptId(Long deptId) {
		this.deptId = deptId;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public Double getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(double totalScore) {
		this.totalScore = totalScore;
	}

}
