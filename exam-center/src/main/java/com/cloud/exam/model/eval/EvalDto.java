package com.cloud.exam.model.eval;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "评估传输对象")
public class EvalDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value="评估分数")
    private int score;

    @ApiModelProperty(value="等级")
    private String level;

    public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public void caculateLevel() {
        if(this.score>=80) {
            this.level = "高";
        } else if(this.score<60) {
            this.level = "低";
        } else {
            this.level = "中";
        }
    }
}
