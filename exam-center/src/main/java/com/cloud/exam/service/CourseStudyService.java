package com.cloud.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.exam.model.course.CourseStudy;

import java.util.Date;
import java.util.List;

/**
 * Created by dyl on 2021/03/22.
 */
public interface CourseStudyService extends IService<CourseStudy> {


    List<CourseStudy> findCourseByStudentId(Date d1, Date d2, Long id);

    CourseStudy copyCourse(CourseStudy courseStudyDetail,Long id);

    void copyCourse1(CourseStudy courseStudyDetail);
}
