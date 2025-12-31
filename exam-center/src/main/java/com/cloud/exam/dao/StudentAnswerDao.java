package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.exam.model.exam.StudentAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * @author md
 * @date 2021/3/29 15:22
 */
@Mapper
public interface StudentAnswerDao extends BaseMapper<StudentAnswer> {

    @Select("select * from student_answer t where t.studentid = #{studentId}")
    List<StudentAnswer> findListByStuId(long studentId);

    @Select("select * from student_answer t where t.student_id = #{studentId} and t.paper_id=#{paperId}")
    List<StudentAnswer> findListByStuIdAndPaperId(Long studentId,Long paperId);
}
