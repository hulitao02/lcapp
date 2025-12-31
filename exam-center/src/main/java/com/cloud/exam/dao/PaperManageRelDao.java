package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.exam.model.exam.PaperManageRel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PaperManageRelDao extends BaseMapper<PaperManageRel> {
    @Select("select * from paper_manage_rel")
    List<PaperManageRel> findAll();

    @Select("select * from paper_manage_rel where paper_id = #{paperId} order by sort asc")
    List<PaperManageRel> findByPaperId(Long paperId);

    @Insert("insert into paper_manage_rel (paper_id,question_id,score,score_basis,sort,question_time) values " +
            "(#{paperId},#{questionId},#{score},#{scoreBasis},#{sort},#{questionTime})")
    int add(PaperManageRel paperManage);

    @Delete("delete from paper_manage_rel t where t.paper_id = #{paperId} ")
    int deleteByPaperId(long paperId);
}
