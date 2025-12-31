package com.cloud.exam.model.exam;

import lombok.Data;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author tongkesong
 * @since 2023-09-04
 */
@Data
public class QuestionTransfer {
    private List<Question> questionList;
    private List<QuestionManage> questionManageList;
    private List<Long> questionManageIdList;
}
