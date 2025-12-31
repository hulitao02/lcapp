package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.CourseKpRelDao;
import com.cloud.exam.model.course.CourseKpRel;
import com.cloud.exam.service.CourseKpRelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dyl on 2021/03/22.
 */
@Service
@Transactional
public class CourseKpRelImpl extends ServiceImpl<CourseKpRelDao,CourseKpRel> implements CourseKpRelService {

    @Autowired
    private CourseKpRelDao courseKpRelDao;


}
