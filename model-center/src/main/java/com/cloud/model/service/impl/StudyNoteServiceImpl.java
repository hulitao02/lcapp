package com.cloud.model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.model.bean.dto.StudyNotesDto;
import com.cloud.model.dao.StudyNoteDao;
import com.cloud.model.model.StudyNotes;
import com.cloud.model.service.StudyNoteService;
import com.cloud.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
public class StudyNoteServiceImpl extends ServiceImpl<StudyNoteDao, StudyNotes>
        implements StudyNoteService {


    @Autowired
    private StudyNoteDao studyNoteDao;

    /**
     * 查询 某个用户的学习笔记
     *
     * @param studyNoteDto
     * @return
     */
    @Override
    public IPage<StudyNotesDto> getStudyNoteListPage(StudyNotesDto studyNoteDto) {

        IPage pageParam = new Page();
        pageParam.setCurrent(Objects.isNull(studyNoteDto.getPageNum()) ? 1 : studyNoteDto.getPageNum());
        pageParam.setSize(Objects.isNull(studyNoteDto.getPageSize()) ? 10 : studyNoteDto.getPageSize());

        /**
         *直接调用
         */
        QueryWrapper<StudyNotesDto> queryWrapper = new QueryWrapper();
        if (Objects.nonNull(studyNoteDto.getUserId())) {
            queryWrapper.eq("user_id", studyNoteDto.getUserId());
        }
        if (Objects.nonNull(studyNoteDto.getKnId())) {
            queryWrapper.eq("kn_id", studyNoteDto.getKnId());
        }

        if (Objects.nonNull(studyNoteDto.getModelKpId())) {
            queryWrapper.eq("model_kp_id", studyNoteDto.getModelKpId());
        }
        /**
         *  直接使用like查询
         */
        if (StringUtils.isNotBlank(studyNoteDto.getQueryParams())) {
//          查询的条件
            String queryParams = studyNoteDto.getQueryParams();
            queryWrapper.and(wrapper -> wrapper.like("notes_info", queryParams));
        }
        IPage studyNoteListPage = this.studyNoteDao.getStudyNoteListPage(pageParam, queryWrapper,studyNoteDto);
        return studyNoteListPage;
    }
}
