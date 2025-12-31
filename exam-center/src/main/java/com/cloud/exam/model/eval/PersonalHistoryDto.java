package com.cloud.exam.model.eval;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
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
public class PersonalHistoryDto {
    private List<String> legend = Arrays.asList("分数");
    @JsonProperty("xAxis")
    private List<String> xAxis = new ArrayList<>();
    private List<Series> series = new ArrayList<>();

    @Data
    public static class Series {
        private String name = "分数";
        private List<String> data = new ArrayList<>();
    }
}
