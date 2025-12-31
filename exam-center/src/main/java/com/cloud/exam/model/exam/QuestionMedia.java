package com.cloud.exam.model.exam;

import com.alibaba.fastjson.JSON;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author tongkesong
 * @since 2024-06-24
 */
@Data
public class QuestionMedia {
    private String text;
    private String[] url;

    public QuestionMedia(String text) {
        this.text = text;
        this.url = new String[0];
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
