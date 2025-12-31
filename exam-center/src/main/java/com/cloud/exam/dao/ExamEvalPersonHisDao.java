package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.exam.model.eval.ExamEvalPerson;
import com.cloud.exam.model.eval.ExamEvalPersonHis;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface ExamEvalPersonHisDao extends BaseMapper<ExamEvalPersonHis> {

}