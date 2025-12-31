package com.cloud.model.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.cloud.model.bean.dto.StudyNotesDto;
import com.cloud.model.model.StudyNotes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface StudyNoteDao extends BaseMapper<StudyNotes> {

    /**
     * 分页查询
     *
     * @return
     */
    public IPage<StudyNotesDto> getStudyNoteListPage(IPage<StudyNotesDto> pageParams, @Param(Constants.WRAPPER) Wrapper wrapper,
                                                     @Param("dto") StudyNotesDto studyNoteDto);


    /**
     * @author: 胡立涛
     * @description: TODO 根据知识删除studyNode信息
     * @date: 2022/5/28
     * @param: [kIds]
     * @return: void
     */
    void delStudyNode(@Param("kIds") String[] kIds);


}
