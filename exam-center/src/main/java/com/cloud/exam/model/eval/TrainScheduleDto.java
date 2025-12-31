package com.cloud.exam.model.eval;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author tongkesong
 * @since 2023-09-10
 */
@Data
public class TrainScheduleDto {
    private String month;
    private Integer kpNum;
    private String kpIds;
    private String kpNames;
}
