package com.cloud.model.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.bean.dto.StudyNotesDto;
import com.cloud.model.model.StudyNotes;

public interface StudyNoteService extends IService<StudyNotes> {

    /**
     * 分页查询
     *
     * @param studyNoteDto
     * @return
     */
    public IPage<StudyNotesDto> getStudyNoteListPage(StudyNotesDto studyNoteDto);


}
