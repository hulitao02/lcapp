package com.cloud.model.common;

/**
 * <p>
 *
 * </p>
 *
 * @author tongkesong
 * @since 2023-09-04
 */
public class ExamConstant {
    public static final Integer QUESTION_USED = 1;
    public static final Integer QUESTION_NOT_USED = 0;

    public static boolean isQuestionUsed(Integer status) {
        return QUESTION_USED.equals(status);
    }
}
