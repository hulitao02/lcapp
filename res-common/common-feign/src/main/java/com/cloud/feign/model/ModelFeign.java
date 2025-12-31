package com.cloud.feign.model;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


@Component
@FeignClient(name = "model-center")
public interface ModelFeign {


    @ApiOperation("统计学习知识数量前10名的学员排名【名称：年度学习排名】")
    @RequestMapping(value = "/statistics/getUserStudyCountGroupbyUId",method = RequestMethod.GET)
    public List<Map<String, Object>> getUserStudyCountGroupbyUId(@RequestParam("paramDate") String paramDate);


    @ApiOperation("统计 被学习的知识次数最多的知识和，人数 [名称：热点知识]")
    @RequestMapping(value = "/statistics/getTopKnAndUserCountGroupbyKnId",method = RequestMethod.GET)
    public List<Map<String, Object>> getKnCountGroupbyKnId(@RequestParam(value = "paramDate",required = false) String paramDate);

    @ApiOperation("推荐知识分页列表")
    @RequestMapping(value = "/knowledge/listKnowledgePage",method = RequestMethod.GET)
    public Map listKnowledgePage(@RequestParam(value = "pageNo", required = true) int pageNo,
                                       @RequestParam(value = "pageSize", required = true) int pageSize,
                                       @RequestParam(value = "userId") int userId,
                                       @RequestParam(value = "sensesName", required = false) String sensesName) ;

    @ApiOperation("根据知识点推荐知识分页列表")
    @RequestMapping(value = "/knowledge/listKnowledgePage",method = RequestMethod.GET)
    public Map listKnowledgePage(@RequestParam(value = "pageNo", required = true) int pageNo,
                                 @RequestParam(value = "pageSize", required = true) int pageSize,
                                 @RequestParam(value = "userId") int userId,
                                 @RequestParam(value = "kpIds") String kpIds,
                                 @RequestParam(value = "sensesName", required = false) String sensesName) ;

}
