package com.cloud.model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.model.bean.dto.QuestionDto;
import com.cloud.model.dao.QuestionDao;
import com.cloud.model.model.Question;
import com.cloud.model.service.QuestionService;
import com.cloud.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionDao, Question> implements QuestionService {

    /**
     * 问题Dao层
     */
    @Autowired
    private QuestionDao questionDao;

    /**
     * @param questionDto
     * @return
     */
    @Override
    public IPage<QuestionDto> getIPageQuestList(QuestionDto questionDto) {
        /**
         *  分页查询问题的信息
         */
        IPage<QuestionDto> pageParams = new Page<>();
        pageParams.setCurrent(questionDto.getPageNum());
        pageParams.setSize(questionDto.getPageSize());
        /**
         *  mybatis-plus 拼接where参数
         */
        QueryWrapper<QuestionDto> queryWrapper = new QueryWrapper<QuestionDto>();
        if (Objects.nonNull(questionDto.getUserId())) {
            queryWrapper.eq("user_id", questionDto.getUserId());
        }
        /**
         *  直接使用like查询
         */
        if(StringUtils.isNotBlank(questionDto.getQueryParams())){
//          查询的条件
            String queryParams = questionDto.getQueryParams();
            queryWrapper.and(wrapper->wrapper.like("kn_name",queryParams)
                    .or().like("question_title",queryParams));

        }

        queryWrapper.orderByDesc("create_time");
        IPage<QuestionDto> questionDtoIPage = this.questionDao.getQuestionListPage(pageParams, queryWrapper);
        return questionDtoIPage;
    }


}
