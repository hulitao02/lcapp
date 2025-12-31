package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.StudentAnswerDao;
import com.cloud.exam.model.exam.StudentAnswer;
import com.cloud.exam.service.StudentAnswerService;
import com.cloud.utils.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author md
 * @date 2021/3/29 16:46
 */
@Service
public class StudentAnswerServiceImpl extends ServiceImpl<StudentAnswerDao, StudentAnswer> implements StudentAnswerService {
    @Resource
    private StudentAnswerDao studentAnswerDao;

    @Override
    public List<StudentAnswer> findListByStuId(long studentId) {
        return studentAnswerDao.findListByStuId(studentId);
    }

    @Override
    public List<StudentAnswer> findListByStuIdAndPaperId(Long studentId, Long paperId) {
        return studentAnswerDao.findListByStuIdAndPaperId(studentId,paperId);
    }

    @Override
    public Map<Long, StudentAnswer> findMapByStuIdAndPaperId(Long studentId, Long paperId) {
        List<StudentAnswer> studentAnswerList = this.findListByStuIdAndPaperId(studentId, paperId);
        Map<Long, StudentAnswer> resultMap=new HashMap<>(studentAnswerList.size());
        if (ObjectUtils.isNotNull(studentAnswerList)){
            studentAnswerList.forEach(studentAnswer -> {
                resultMap.put(studentAnswer.getQuestionId(),studentAnswer);
            });
        }
        return resultMap;
    }
}
