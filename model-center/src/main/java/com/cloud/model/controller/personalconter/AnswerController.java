package com.cloud.model.controller.personalconter;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.bean.dto.QuestionDto;
import com.cloud.model.dao.QuestionDao;
import com.cloud.model.model.Answer;
import com.cloud.model.model.AnswerWeb;
import com.cloud.model.service.AnswerService;
import com.cloud.model.service.QuestionService;
import com.cloud.model.user.AppUser;
import com.cloud.model.user.LoginAppUser;
import com.cloud.model.utils.AppUserUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ApiModel(value = "问题controller")
@RestController
@RequestMapping("/answer")
@Slf4j
@RefreshScope
public class AnswerController {


    @Autowired
    private AnswerService answerService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    QuestionDao questionDao;
    @Autowired
    SysDepartmentFeign sysDepartmentFeign;

   /**
    *
    * @author:胡立涛
    * @description: TODO 专家答疑列表查询
    * @date: 2022/1/20
    * @param: [questionDto]
    * @return: com.cloud.core.ApiResult
    */
    @ApiOperation("专家答疑查询")
    @PostMapping("/getQuestionAllList")
    public ApiResult getQuestionAllList(@RequestBody QuestionDto questionDto) {
        try {
            // 当前登录用户
            LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
            IPage<QuestionDto> iPageQuestList = this.questionService.getIPageQuestList(questionDto);
            if (iPageQuestList!=null){
                List<QuestionDto> records = iPageQuestList.getRecords();
                for (QuestionDto bean:records){
                    bean.setIsAnswer(0);
                    // 查询当前登录人是否回答过该问题
                    Map parMap=new HashMap();
                    parMap.put("userId",loginAppUser.getId());
                    parMap.put("questionId",bean.getId());
                    Map answer = questionDao.getAnswer(parMap);
                    if (answer!=null){
                        bean.setIsAnswer(1);
                    }
                    // 查询专家所在部门信息
                    AppUser appUserById = sysDepartmentFeign.findAppUserById(Long.valueOf(bean.getUserId()));
                    bean.setUserName(appUserById.getUsername());
                    bean.setDepartName(appUserById.getDepartmentName());
                }
            }

            return ApiResultHandler.buildApiResult(200, "专家答疑列表查询", iPageQuestList);
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     *
     * @author:胡立涛
     * @description: TODO 点击问题 查看所有专家解答列表
     * @date: 2022/1/20
     * @param: [questionId]
     * @return: com.cloud.core.ApiResult
     */
    @ApiOperation("通过Id 查询所有的回复记录")
    @GetMapping("/getAnswersByQuestionId")
    public ApiResult getAnswersByQuestionId(@RequestParam("questionId") Integer questionId) {
        try {
            List<AnswerWeb> rList=new ArrayList<>();
            QueryWrapper<Answer> queryWrapper = new QueryWrapper();
            queryWrapper.eq("question_id", questionId);
            queryWrapper.orderByDesc("create_time");
            List<Answer> answerList = this.answerService.list(queryWrapper);
            for (Answer bean:answerList){
                AnswerWeb answerWeb=new AnswerWeb();
                BeanUtils.copyProperties(bean,answerWeb);
                AppUser appUserById = sysDepartmentFeign.findAppUserById(Long.valueOf(bean.getUserId()));
                answerWeb.setUserName(appUserById.getUsername());
                answerWeb.setDeptName(appUserById.getDepartmentName());
                rList.add(answerWeb);
            }
            return ApiResultHandler.buildApiResult(200, "查询问题的回复列表", rList);
        }catch (Exception e){
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }

   /**
    *
    * @author:胡立涛
    * @description: TODO 保存专家答案
    * @date: 2022/1/20
    * @param: [answer]
    * @return: com.cloud.core.ApiResult
    */
    @ApiOperation("保存专家回答")
    @PostMapping("/submitAnswers")
    public ApiResult submitAnswers(@RequestBody Answer answer) {
        log.info("answer/submitAnswers#  --> params:answer {}", JSON.toJSONString(answer));
        try {
            answer.setCreateTime(new Timestamp(System.currentTimeMillis()));
            this.answerService.saveOrUpdate(answer);
        }catch (Exception e){
            e.printStackTrace();
            log.error("answer/submitAnswers#  --> exception {}", e.getMessage());
            return ApiResultHandler.buildApiResult(500, "提交解答异常", e.getMessage());
        }
        return ApiResultHandler.buildApiResult(200, "提交解答成功", null);
    }
}
