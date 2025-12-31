package com.cloud.exam.model.eval;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 *
 * </p>
 *
 * @author tongkesong
 * @since 2023-09-10
 */
@Data
public class EvalNewDto {
    @ApiModelProperty(value = "评估分数")
    private BigDecimal bigDecimalScore;

    public String getScore() {
        if (bigDecimalScore == null) {
            return null;
        }
        BigDecimal score = bigDecimalScore.setScale(2, BigDecimal.ROUND_HALF_UP);
        return score.toString();
    }

    public String getLevel() {
        double score;
        if (this.bigDecimalScore == null) {
            return null;
        } else if ((score = this.bigDecimalScore.doubleValue()) >= 80) {
            return "高";
        } else if (score < 60) {
            return "低";
        } else {
            return "中";
        }
    }

}
