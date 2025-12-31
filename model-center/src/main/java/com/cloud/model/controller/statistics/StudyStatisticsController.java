package com.cloud.model.controller.statistics;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.controller.CommonConstans;
import com.cloud.model.dao.KnowledgeViewDao;
import com.cloud.model.feign.UserRoleFeign;
import com.cloud.model.service.CollectKnowledgeService;
import com.cloud.utils.CollectionsCustomer;
import com.cloud.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import tool.PooledHttpClientAdaptor;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiModel("学习模块 统计处理类")
@Slf4j
@RestController
@RequestMapping(value = "/statistics")
public class StudyStatisticsController {

    // 知识图谱访问地址
    @Value(value = "${tupu_server}")
    private String tupuServer;

    @Autowired
    private CollectKnowledgeService collectKnowledgeService;

    @Autowired
    private UserRoleFeign userRoleFeign;

    @Autowired
    private SysDepartmentFeign sysDepartmentFeign;
    @Autowired
    KnowledgeViewDao knowledgeViewDao;


    @ApiOperation("统计学习知识数量前10名的学员排名【名称：年度学习排名】")
    @RequestMapping(value = "/getUserStudyCountGroupbyUId",method = RequestMethod.GET)
    public List<Map<String, Object>> getUserStudyCountGroupbyUId(@RequestParam("paramDate") String paramDate) {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("paramsDate",paramDate);
            List<Map<String, Object>> userCountList = knowledgeViewDao.getUserStudyCountGroupbyUId(paramMap);
//            MAP中KEY值 转小写
//            userCountList = CollectionsCustomer.builder().build().listMapToLowerCase(userCountList);
            for (Map map:userCountList){
                try{
                    map.put("userName", sysDepartmentFeign.findAppUserById(Long.valueOf(map.get("userid").toString())).getNickname());
                }catch (Exception e){
                    map.put("userName", map.get("userid"));
                }
            }
            return userCountList;
    }

    /**
     *  统计 某个知识点
     * @param paramDate
     * @return
     */
    @ApiOperation("统计 被学习的知识次数最多的知识，人数 名称：热点知识")
    @RequestMapping(value = "/getTopKnAndUserCountGroupbyKnId",method = RequestMethod.GET)
    public List<Map<String, Object>> getKnCountGroupbyKnId(@RequestParam(value = "paramDate",required = false) String paramDate) {

        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("paramsDate",paramDate);
        List<Map<String, Object>> knAndUserContMap = this.knowledgeViewDao.getUserCountByKn(paramMap);
//        knAndUserContMap = CollectionsCustomer.builder().build().listMapToLowerCase(knAndUserContMap);
        return knAndUserContMap;

    }


    /**
     *
     * @author:胡立涛
     * @description: TODO 后台管理：知识被学习情况统计
     * @date: 2022/2/14
     * @param: []
     * @return: com.cloud.core.ApiResult
     */
    @GetMapping(value = "studyKnowledge")
    public ApiResult studyKnowledge(){
        try {
            Map<String,Object> rMap=new HashMap<>();
            String url = tupuServer + CommonConstans.getKnowledgeTotal;
            PooledHttpClientAdaptor adaptor = new PooledHttpClientAdaptor();
            Map<String, String> headMap = new HashMap<>();
            //知识点下的所有的知识
            String result = adaptor.doGet(url, headMap, Collections.emptyMap());
            if (StringUtils.isNotEmpty(result)) {
                JSONObject esObject = JSON.parseObject(result);
                String code = esObject.getString("code");
                if (code.equals("200")) {
                    rMap.put("knowledgeCount",esObject.get("result"));
                } else {
                    return ApiResultHandler.buildApiResult(500, "第三方接口异常", null);
                }
            }
            // 查询已学知识数量
            List<Map> studyKnowledgeCount = knowledgeViewDao.getStudyKnowledgeCount();
            rMap.put("studyKnowledgeCount",studyKnowledgeCount==null?0:studyKnowledgeCount.size());
            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
        }catch (Exception e){
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }

    }
}
