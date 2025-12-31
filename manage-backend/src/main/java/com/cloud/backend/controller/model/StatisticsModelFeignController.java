package com.cloud.backend.controller.model;

import com.cloud.feign.model.ModelFeign;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@ApiModel("学习模块 model controller")
@Slf4j
@RequestMapping("/model")
public class StatisticsModelFeignController {

    @Autowired
    private ModelFeign modelFeign;

    @ApiOperation("统计学习知识数量前10名的学员排名【名称：年度学习排名】")
    @RequestMapping(value = "/statistics/getUserStudyCountGroupbyUId",method = RequestMethod.GET)
    public List<Map<String, Object>> getUserStudyCountGroupbyUId(@RequestParam("paramDate") String paramDate){
        return this.modelFeign.getUserStudyCountGroupbyUId(paramDate);
    };


    @ApiOperation("统计 被学习的知识次数最多的知识和，人数 名称：热点知识")
    @RequestMapping(value = "/statistics/getTopKnAndUserCountGroupbyKnId",method = RequestMethod.GET)
    public List<Map<String, Object>> getKnCountGroupbyKnId(@RequestParam(value = "paramDate",required = false) String paramDate){
        return this.modelFeign.getKnCountGroupbyKnId(paramDate);
    };


}
