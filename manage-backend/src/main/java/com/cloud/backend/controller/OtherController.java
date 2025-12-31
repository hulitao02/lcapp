package com.cloud.backend.controller;

import com.cloud.backend.dao.KnowledgePointsDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class OtherController {

    @Autowired
    KnowledgePointsDao knowledgePointsDao;

    /**
     * @author:胡立涛
     * @description: TODO 根据知识点查询该知识点的下一级知识点信息
     * @date: 2022/6/28
     * @param: [map]
     * @return: java.util.Map
     */
    @PostMapping(value = "/other/getNextPoint")
    public List<Map> getNextPoint(@RequestBody Map<String, Object> map) {
        List<Map> nextPoint = knowledgePointsDao.getNextPoint(map);
        return nextPoint;
    }

    /**
     *
     * @author:胡立涛
     * @description: TODO 根据知识点名称关键字，返回知识点ID的集合
     * @date: 2022/6/28
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/other/getPointListForName")
    public List<Map> getPointListForName(@RequestBody Map<String, Object> map) {
        String pointName=map.get("pointName").toString();
        pointName="%"+pointName+"%";
        map.put("pointName",pointName);
        List<Map> nextPoint = knowledgePointsDao.getPointListForName(map);
        return nextPoint;
    }



}
