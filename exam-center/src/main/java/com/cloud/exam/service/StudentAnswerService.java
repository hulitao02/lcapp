package com.cloud.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.exam.model.exam.StudentAnswer;

import java.util.List;
import java.util.Map;

/**
 * @author md
 * @date 2021/3/29 15:27
 */
public interface StudentAnswerService extends IService<StudentAnswer> {
    List<StudentAnswer> findListByStuId(long studentId);

    List<StudentAnswer> findListByStuIdAndPaperId(Long studentId,Long paperId);

    Map<Long,StudentAnswer> findMapByStuIdAndPaperId(Long studentId,Long paperId);
}
