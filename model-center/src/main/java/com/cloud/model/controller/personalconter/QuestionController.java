package com.cloud.model.controller.personalconter;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.model.bean.dto.QuestionDto;
import com.cloud.model.model.Question;
import com.cloud.model.service.QuestionService;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.Objects;


@ApiModel(value = "问题controller")
@RestController
@RequestMapping("/question")
@Slf4j
@RefreshScope
public class QuestionController {


    @Autowired
    private QuestionService questionService;
    /**
     * 提问
     *
     * @return
     */
    @ApiOperation("提交问题")
    @PostMapping("/submitQuestion")
    public ApiResult getQuestionListById(@RequestBody Question question) {
        log.info("question/submitQuestion#  --> params:question {}", JSON.toJSONString(question));
        if (Objects.isNull(question)) {
            return ApiResultHandler.buildApiResult(400, "请完整的提交问题", question);
        }
        try {
            question.setCreateTime(new Timestamp(System.currentTimeMillis()));
            this.questionService.saveOrUpdate(question);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("question/submitQuestion#  --> params:question {}", JSON.toJSONString(question));
            return ApiResultHandler.buildApiResult(500, "提交问题异常", e.getMessage());
        }
        return ApiResultHandler.buildApiResult(200, "提交问题成功!", question);
    }

    /**
     *
     * @author:胡立涛
     * @description: TODO 我的问题列表查询
     * @date: 2022/1/20
     * @param: [questionDto]
     * @return: com.cloud.core.ApiResult
     */
    @ApiOperation("查询问题分页信息")
    @PostMapping("/getPageForQuestion")
    public ApiResult getPageForQuestion(@RequestBody QuestionDto questionDto) {
        try {
            IPage<QuestionDto> resultPage = new Page<>();
            resultPage.setCurrent(Objects.isNull(questionDto.getPageNum())?1:questionDto.getPageNum());
            resultPage.setSize(questionDto.getPageSize());
            resultPage = this.questionService.getIPageQuestList(questionDto);
            return ApiResultHandler.buildApiResult(200, String.format("用户：%s 问题列表返回成功",questionDto.getUserId()), resultPage);
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }
}
