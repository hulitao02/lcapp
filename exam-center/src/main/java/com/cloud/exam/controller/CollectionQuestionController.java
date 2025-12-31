package com.cloud.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.exam.dao.CollectionQuestionDao;
import com.cloud.exam.model.exam.CollectionQuestion;
import com.cloud.exam.service.CollectionQuestionService;
import com.cloud.exam.service.QuestionService;
import com.cloud.exam.utils.CommonPar;
import com.cloud.model.user.LoginAppUser;
import com.cloud.model.utils.AppUserUtil;
//import com.cloud.utils.CollectionsCustomer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author:胡立涛
 * @description: TODO 试题收藏
 * @date: 2022/8/18
 * @param:
 * @return:
 */
@RestController
@RequestMapping(value = "/collquestion")
@RefreshScope
@Slf4j
public class CollectionQuestionController {

    @Autowired
    CollectionQuestionDao collectionQuestionDao;
    @Autowired
    CollectionQuestionService collectionQuestionService;
    @Autowired
    QuestionService questionService;


    /**
     * @author:胡立涛
     * @description: TODO
     * @date: 2022/8/18
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "saveInfo")
    public ApiResult saveInfo(@RequestBody Map<String, Object> map) {
        try {
            if (map.get("userId") == null) {
                return ApiResultHandler.buildApiResult(100, "参数userId为空", null);
            }
            if (map.get("questionId") == null) {
                return ApiResultHandler.buildApiResult(100, "参数questionId为空", null);
            }
            if (map.get("question") == null) {
                return ApiResultHandler.buildApiResult(100, "参数question为空", null);
            }
            Integer typeId = map.get("typeId") == null ? null : Integer.parseInt(map.get("typeId").toString());
            String typeName = map.get("type") == null ? null : map.get("type").toString();
            if (typeName == null && typeId == null) {
                return ApiResultHandler.buildApiResult(100, "参数type、typeId为空", null);
            }
            if (typeName == null) {
                typeName = CommonPar.question_type.get(typeId.toString()).toString();
            }
            Integer questionFlg = map.get("questionFlg") == null ? null : Integer.parseInt(map.get("questionFlg").toString());
            Map<String, Object> parMap = new HashMap<>();
            parMap.put("userId", Long.valueOf(map.get("userId").toString()));
            parMap.put("questionId", Long.valueOf(map.get("questionId").toString()));
//            parMap.put("question", map.get("question").toString());
            if (questionFlg != null) {
                parMap.put("questionFlg", questionFlg);
            }
//            map.put("type", typeName);
            // 查看是否收藏过该题
            List<Map<String, Object>> list = collectionQuestionDao.findByPar(parMap);
//            list = CollectionsCustomer.builder().build().listMapToLowerCase(list);

            if (list == null || list.size() == 0) {
                CollectionQuestion collectionQuestion = new CollectionQuestion();
                collectionQuestion.setUserId(Long.valueOf(map.get("userId").toString()));
                collectionQuestion.setQuestionId(Long.valueOf(map.get("questionId").toString()));
                collectionQuestion.setQuestion(map.get("question").toString());
                collectionQuestion.setType(typeName);
                // 试题来源 1：试题库 2：试卷
                collectionQuestion.setQuestionFlg(2);
                if (questionFlg != null) {
                    collectionQuestion.setQuestionFlg(questionFlg);
                }
                collectionQuestion.setCreateTime(new Timestamp(System.currentTimeMillis()));
                collectionQuestionDao.insert(collectionQuestion);
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 取消收藏试题
     * @date: 2022/8/18
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "delInfo")
    public ApiResult delInfo(@RequestBody Map<String, Object> map) {
        try {
            if (map.get("id") == null) {
                return ApiResultHandler.buildApiResult(100, "参数id为空", null);
            }
            collectionQuestionDao.delInfo(map);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 试题收藏列表
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
            Page<CollectionQuestion> pg = new Page(page, size);
            QueryWrapper queryWrapper = new QueryWrapper();
            LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
            queryWrapper.eq("user_id", loginAppUser.getId());
            if (keyWord != null) {
                keyWord = "%" + keyWord + "%";
                queryWrapper.like("question", keyWord);
            }
            queryWrapper.orderByDesc("create_time");
            IPage pageResult = collectionQuestionService.page(pg, queryWrapper);
            return ApiResultHandler.buildApiResult(200, "操作成功", pageResult);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据用户id，试题id查询是否收藏
     * @date: 2022/8/24
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getCollectionId")
    public ApiResult getCollectionId(@RequestBody Map<String, Object> map) {
        try {
            if (map.get("userId") == null) {
                return ApiResultHandler.buildApiResult(100, "参数userId为空", null);
            }
            if (map.get("questionId") == null) {
                return ApiResultHandler.buildApiResult(100, "参数questionId为空", null);
            }
            Integer questionFlg = map.get("questionFlg") == null ? null : Integer.parseInt(map.get("questionFlg").toString());
            QueryWrapper<CollectionQuestion> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", Long.valueOf(map.get("userId").toString()));
            queryWrapper.eq("question_id", Long.valueOf(map.get("questionId").toString()));
            if (questionFlg != null) {
                queryWrapper.eq("question_flg", questionFlg);
            } else {
                queryWrapper.eq("question_flg", 2);
            }
            CollectionQuestion one = collectionQuestionService.getOne(queryWrapper);
            Long collectionId = one == null ? 0L : one.getId();
            return ApiResultHandler.buildApiResult(200, "操作成功", collectionId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }
}
