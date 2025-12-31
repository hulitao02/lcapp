package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.exam.model.course.CourseStudentAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * @author md
 * @date 2021/3/29 15:22
 */
@Mapper
public interface CourseStudentAnswerDao extends BaseMapper<CourseStudentAnswer> {

    @Select("select * from course_student_answer t where t.studentid = #{studentId}")
    List<CourseStudentAnswer> findListByStuId(long studentId);

    @Select("select * from course_student_answer t where t.student_id = #{studentId} and t.course_id=#{courseId}")
    List<CourseStudentAnswer> findListByStuIdAndPaperId(Long studentId, Long courseId);
}
