package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.CourseQuestionRelDao;
import com.cloud.exam.model.course.CourseQuestionRel;
import com.cloud.exam.service.CourseQuestionRelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dyl on 2021/03/22.
 */
@Service
@Transactional
public class CourseQuestionRelImpl extends ServiceImpl<CourseQuestionRelDao,CourseQuestionRel> implements CourseQuestionRelService {

    @Autowired
    private CourseQuestionRelDao courseQuestionRelDao;


}
