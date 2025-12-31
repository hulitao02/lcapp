package com.cloud.model.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.model.dao.ModelKpDao;
import com.cloud.model.model.ModelKp;
import com.cloud.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/auto")
@ApiModel(value = "自动出题")
@Slf4j
@RefreshScope
public class AutoQuestionController {

    @Autowired
    ModelKpDao modelKpDao;
    /**
     *
     * @author:胡立涛
     * @description: TODO 自动出题
     * @date: 2022/11/8
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "autoQuestion")
    public ApiResult autoQuestion(@RequestBody Map<String,Object> map){
        try {
            // 试题类型 1：单选 2：多选 3：判断 4：填空
            Long questionType=map.get("questionType")==null?null:Long.valueOf(map.get("questionType").toString());
            // 知识点
            String kpIds=map.get("kpIds")==null?null:map.get("kpIds").toString();
            // 难度系数
            Double difficulty=map.get("difficulty")==null?null:Double.valueOf(map.get("difficulty").toString());
            // 题数
            Integer questionNum=map.get("questionNum")==null?null:Integer.valueOf(map.get("questionNum").toString());
            // 试题模板
            Integer modelType=map.get("modelType")==null?null:Integer.valueOf(map.get("modelType").toString());

            if (questionType==null){
                return ApiResultHandler.buildApiResult(100, "参数questionType为空", null);
            }
            if (kpIds==null){
                return ApiResultHandler.buildApiResult(100, "参数kpIds为空", null);
            }
            if (difficulty==null){
                return ApiResultHandler.buildApiResult(100, "参数difficulty为空", null);
            }
            if (questionNum==null){
                return ApiResultHandler.buildApiResult(100, "参数questionNum为空", null);
            }
            if (modelType==null){
                return ApiResultHandler.buildApiResult(100, "参数modelType为空", null);
            }
            // 查询完成数据对接的知识点
            QueryWrapper<ModelKp> queryWrapper=new QueryWrapper<>();
            queryWrapper.select("DISTINCT kp_id");
            queryWrapper.eq("status",2);
            List<ModelKp> modelKps = modelKpDao.selectList(queryWrapper);
            Map<String,Object> kpMap=new HashMap<>();
            for (ModelKp modelKp:modelKps){
                kpMap.put(modelKp.getKpId().toString(),1);
            }
            List<Long> kpList=new ArrayList<>();
            String[] kpIdArr=kpIds.split(",");
            for (String str:kpIdArr){
                if (!StringUtils.isEmpty(str)){
                    if (kpMap.get(str)!=null){
                        kpList.add(Long.valueOf(str));
                    }
                }
            }

            //  单选题 1：下图中军用机场是什么型号 2：下面哪张图是安德森空军基地3：两种模板都有
            // 当前题目数
            int currentNum=0;
            if (questionType==1){
                // 知识点名称做为选项
                if (modelType==1){
                    for (Long kpId:kpList){

                    }
                }
            }





            System.out.println("---------:"+modelKps);
            return ApiResultHandler.buildApiResult(200, "操作成功", modelKps);
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "自动出题异常", e.toString());
        }
    }
}
