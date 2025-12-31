package com.cloud.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.exam.model.exam.CollectionQuestion;
import com.cloud.exam.model.exam.ErrorQuestion;
import com.cloud.exam.service.CollectionQuestionService;
import com.cloud.exam.service.ErrorQuestionService;
import com.cloud.model.user.LoginAppUser;
import com.cloud.model.utils.AppUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


/**
 * @author:胡立涛
 * @description: TODO 错误试题模块
 * @date: 2022/8/19
 * @param:
 * @return:
 */
@RestController
@RequestMapping(value = "/errorquestion")
@RefreshScope
@Slf4j
public class ErrorQuestionController {

    @Autowired
    ErrorQuestionService errorQuestionService;
    @Autowired
    CollectionQuestionService collectionQuestionService;

    /**
     * @author:胡立涛
     * @description: TODO 错误试题列表（分页）
     * @date: 2022/8/19
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getList")
    public ApiResult getList(@RequestBody Map<String, Object> map) {
        try {
            if (map.get("page") == null) {
                return ApiResultHandler.buildApiResult(100, "参数page为空", null);
            }
            if (map.get("size") == null) {
                return ApiResultHandler.buildApiResult(100, "参数size为空", null);
            }
            int page = Integer.parseInt(map.get("page").toString());
            int size = Integer.parseInt(map.get("size").toString());
            String keyWord = map.get("keyWord") == null ? null : map.get("keyWord").toString();
            Page<ErrorQuestion> pg = new Page(page, size);
            QueryWrapper queryWrapper = new QueryWrapper();
            LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
            queryWrapper.eq("user_id", loginAppUser.getId());
            if (keyWord != null) {
                keyWord = "%" + keyWord + "%";
                queryWrapper.like("question", keyWord);
            }
            queryWrapper.orderByDesc("create_time");
            IPage pageResult = errorQuestionService.page(pg, queryWrapper);
            List<ErrorQuestion> recordsList = pageResult.getRecords();
            if (recordsList != null && recordsList.size() > 0) {
                for (ErrorQuestion errorQuestion : recordsList) {
                    // 根据试题id和用户id查询该试题是否收藏
                    QueryWrapper<CollectionQuestion> questionQueryWrapper = new QueryWrapper<>();
                    questionQueryWrapper.eq("user_id", errorQuestion.getUserId());
                    questionQueryWrapper.eq("question_id", errorQuestion.getQuestionId());
                    CollectionQuestion one = collectionQuestionService.getOne(questionQueryWrapper);
                    errorQuestion.setCollectionId(one == null ? 0L : one.getId());
                }
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", pageResult);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据记录id删除错误试题信息
     * @date: 2022/8/24
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "delInfo")
    public ApiResult delInfo(@RequestBody Map<String, Object> map) {
        try {
            if (map.get("id") == null) {
                return ApiResultHandler.buildApiResult(100, "参数id为空", null);
            }
            errorQuestionService.removeById(Long.valueOf(map.get("id").toString()));
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }

}
