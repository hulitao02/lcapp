package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.CourseKpDao;
import com.cloud.exam.model.course.CourseKp;
import com.cloud.exam.service.CourseKpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dyl on 2021/03/22.
 */
@Service
@Transactional
public class CourseKpImpl extends ServiceImpl<CourseKpDao,CourseKp> implements CourseKpService {

    @Autowired
    private CourseKpDao courseKpDao;


}
