package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.exam.model.eval.ExamEvalPerson;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface ExamEvalPersonDao extends BaseMapper<ExamEvalPerson> {

}