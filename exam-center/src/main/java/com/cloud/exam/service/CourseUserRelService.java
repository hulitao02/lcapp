package com.cloud.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.exam.model.course.CourseUserRel;

import java.util.List;


/**
 * Created by dyl on 2021/03/22.
 */
public interface CourseUserRelService extends IService<CourseUserRel> {


    List<Integer> getPerMonthCountByUser(String paramsDate, Long userId);
}
