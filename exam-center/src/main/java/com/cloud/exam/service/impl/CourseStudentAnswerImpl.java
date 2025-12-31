package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.CourseStudentAnswerDao;
import com.cloud.exam.model.course.CourseStudentAnswer;
import com.cloud.exam.service.CourseStudentAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dyl on 2021/03/22.
 */
@Service
@Transactional
public class CourseStudentAnswerImpl extends ServiceImpl<CourseStudentAnswerDao,CourseStudentAnswer> implements CourseStudentAnswerService {

    @Autowired
    private CourseStudentAnswerDao courseStudentAnswerDao;


}
