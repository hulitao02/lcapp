package com.cloud.exam.model.exam;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by dyl on 2021/03/25.
 */

@Data
public class ExamPlaceRel implements Serializable{

    private static final long serialVersionUID = 5358025750160783323L;
    private Long acId;
    private Long placeId;
    private String placeName;
    private Integer seatCount;//座位数量
}
