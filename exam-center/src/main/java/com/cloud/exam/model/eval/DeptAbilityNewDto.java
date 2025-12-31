package com.cloud.exam.model.eval;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author tongkesong
 * @since 2023-09-10
 */
@Data
public class DeptAbilityNewDto {
    @JsonProperty("xAxis")
    private List<String> xAxis = new ArrayList<>();
    private List<List<EvalDeptNewDto>> series = new ArrayList<>();

}