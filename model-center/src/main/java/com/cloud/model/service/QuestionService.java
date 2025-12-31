package com.cloud.model.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.bean.dto.QuestionDto;
import com.cloud.model.model.Question;

public interface QuestionService extends IService<Question> {

    public IPage<QuestionDto> getIPageQuestList(QuestionDto questionDto);


}
