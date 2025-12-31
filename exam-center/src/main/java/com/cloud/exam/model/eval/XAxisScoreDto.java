package com.cloud.exam.model.eval;

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
public class XAxisScoreDto {
    private String xAxis;
    private BigDecimal score;
}
