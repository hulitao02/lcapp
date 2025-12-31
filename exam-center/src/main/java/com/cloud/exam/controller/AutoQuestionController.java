package com.cloud.exam.controller;

import ch.ethz.ssh2.crypto.digest.MAC;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.exam.dao.AutoQuestionKpDao;
import com.cloud.exam.model.exam.AutoQuestionKp;
import com.cloud.exam.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RefreshScope
public class AutoQuestionController {

    private static final Logger logger = LoggerFactory.getLogger(AutoQuestionController.class);

    @Autowired
    AutoQuestionService autoQuestionService;
    @Autowired
    AutoQuestionKpDao autoQuestionKpDao;


    /**
     * @author:胡立涛
     * @description: TODO 自动出题
     * @date: 2022/11/8
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "autoQuestion")
    public ApiResult autoQuestion(@RequestBody Map<String, Object> map) {
        try {
            Map<String, Object> rMap = new HashMap<>();
            // 试题类型 1：单选题
            Integer questionType = map.get("questionType") == null ? null : Integer.valueOf(map.get("questionType").toString());
            // 模板类型 1:下图中驱逐舰是什么型号（）2:
            Integer modelType = map.get("modelType") == null ? null : Integer.valueOf(map.get("modelType").toString());
            // 知识点
            Long kpId = map.get("kpId") == null ? null : Long.valueOf(map.get("kpId").toString());
            // 题目数量
            Integer questionNum = map.get("questionNum") == null ? null : Integer.valueOf(map.get("questionNum").toString());
            // 试题难度
            Double difficulty = map.get("difficulty") == null ? null : Double.valueOf(map.get("difficulty").toString());
            // 关系code
            String relationCode = map.get("relationCode") == null ? null : map.get("relationCode").toString();
            // 关联概念code
            String targetCode = map.get("targetCode") == null ? null : map.get("targetCode").toString();
            // 属性code
            String proCode = map.get("proCode") == null ? null : map.get("proCode").toString();
            if (questionType == null) {
                return ApiResultHandler.buildApiResult(100, "参数questionType为空", null);
            }
            if (modelType == null) {
                return ApiResultHandler.buildApiResult(100, "参数modelType为空", null);
            }
            if (kpId == null) {
                return ApiResultHandler.buildApiResult(100, "参数kpId为空", null);
            }
            if (questionNum == null) {
                return ApiResultHandler.buildApiResult(100, "参数questionNum为空", null);
            }
            if (difficulty == null) {
                return ApiResultHandler.buildApiResult(100, "参数difficulty为空", null);
            }
            if (relationCode == null) {
                return ApiResultHandler.buildApiResult(100, "参数relationCode为空", null);
            }
            if (targetCode == null) {
                return ApiResultHandler.buildApiResult(100, "参数targetCode为空", null);
            }
            if (proCode == null) {
                return ApiResultHandler.buildApiResult(100, "参数proCode为空", null);
            }

            // 题目数量
            int result = 0;
            // 单选题 模板一 下图中XXX是什么型号（） 取知识点
            if (questionType == 1 && modelType == 1) {
                result = autoQuestionService.dxTypeOne(map);
                if (result == -1) {
                    return ApiResultHandler.buildApiResult(100, "该知识点下的知识数量不够", null);
                }
                if (result == -2) {
                    return ApiResultHandler.buildApiResult(100, "图片均已被使用", null);
                }
            }
            // 单选题 模板二 下面哪张图安德森空军基地（） 取知识名称 随即抽取带图片的知识做为正确答案，然后再随即抽取剩下知识中的三张图片
            if (questionType == 1 && modelType == 2) {
                result = autoQuestionService.dxTypeTwo(map);
                if (result == -1) {
                    return ApiResultHandler.buildApiResult(100, "该知识点下的知识数量不够", null);
                }
                if (result == -2) {
                    return ApiResultHandler.buildApiResult(100, "图片均已被使用", null);
                }
                if (result == -3) {
                    return ApiResultHandler.buildApiResult(100, "图片数量不够", null);
                }
            }
            // 判断题
            if (questionType == 3) {
                result = autoQuestionService.pd(map);
                if (result == -1) {
                    return ApiResultHandler.buildApiResult(100, "该知识点下的知识数量不够", null);
                }
            }
            // 填空题
            if (questionType == 4) {
                result = autoQuestionService.tk(map);
                if (result == -1) {
                    return ApiResultHandler.buildApiResult(100, "该知识点下的知识数量不够", null);
                }
            }
            // 多选题
            if (questionType == 2) {
                result = autoQuestionService.duoxuan(map);
                if (result == -1) {
                    return ApiResultHandler.buildApiResult(100, "该知识点下的知识数量不够", null);
                }
            }
            rMap.put("questionType", questionType);
            rMap.put("realNum", result);
            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "自动出题异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 自动出题 图片关系数据
     * @date: 2022/11/14
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getRelationPic")
    public ApiResult getRelationPic(@RequestBody Map<String, Object> map) {
        try {
            Long kpId = map.get("kpId") == null ? null : Long.valueOf(map.get("kpId").toString());
            if (kpId == null) {
                return ApiResultHandler.buildApiResult(100, "参数kpId为空", null);
            }
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("kp_id", kpId);
            AutoQuestionKp autoQuestionKp = autoQuestionKpDao.selectOne(queryWrapper);
            return ApiResultHandler.buildApiResult(200, "操作成功", autoQuestionKp);
        } catch (Exception e) {
            logger.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "自动出题异常", e.toString());
        }
    }
}
