package com.cloud.exam.model.eval;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author tongkesong
 * @since 2023-09-11
 */
@Data
public class EvalDeptNewDto  {
    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "考核次数")
    private Integer khNum;

    private Double score;


}
