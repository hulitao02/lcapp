package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.exam.model.course.CourseStudy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by dyl on 2021/03/22.
 */
@Mapper
public interface CourseStudyDao extends BaseMapper<CourseStudy> {

    List<CourseStudy> findCourseByStudentId(@Param("d1") Date d1,@Param("d2")  Date d2, @Param("userId") Long userId);
    List<CourseStudy> findCourseByStudentIdDM(@Param("d1") Date d1,@Param("d2")  Date d2, @Param("userId") Long userId);
}
