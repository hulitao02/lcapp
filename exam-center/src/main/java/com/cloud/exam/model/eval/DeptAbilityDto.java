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
public class DeptAbilityDto {
    @JsonProperty("xAxis")
    private List<String> xAxis = new ArrayList<>();
    private List<Series> series;

    @Data
    public static class Series {
        private String name;
        private List<SeriesData> data = new ArrayList<>();
    }

    @Data
    public static class SeriesData {
        private String name;
        private String value;
    }
}
