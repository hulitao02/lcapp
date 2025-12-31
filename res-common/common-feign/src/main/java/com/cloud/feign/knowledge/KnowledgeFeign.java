package com.cloud.feign.knowledge;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Component
@FeignClient(name = "knowledge-center")
public interface KnowledgeFeign {

    /**
     * @author:胡立涛
     * @description: TODO 查询根节点下的所有知识点列表
     * @date: 2024/7/17
     * @param: []
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getKnowledgePointListFeign")
    List<Map> getKnowledgePointListFeign() throws Exception;


    /**
     * @author:胡立涛
     * @description: TODO 获取封面图
     * @date: 2024/8/14
     * @param: [map]
     * @return: java.util.Map
     */
    @PostMapping(value = "/knowledgepoints/getPhoto")
    Map getPhoto(@RequestBody Map map) throws Exception;

    /**
     * @author:胡立涛
     * @description: TODO 完成数据对接的知识点树形菜单
     * @date: 2024/7/17
     * @param: [map] ids:多个知识点之间用逗号进行分割
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getKnowledgeBindFeign")
    List<Map> getKnowledgeBind(@RequestBody Map map) throws Exception;

    /**
     * @author:胡立涛
     * @description: TODO 获取根节点信息
     * @date: 2024/7/17
     * @param: []
     * @return: java.util.Map
     */
    @GetMapping(value = "/knowledgepoints/getRootPointFeign")
    Map getRootPoint() throws Exception;


    /**
     * @author:胡立涛
     * @description: TODO 根据知识点查询知识点信息
     * @date: 2024/7/17
     * @param: [id]
     * @return: java.util.Map
     */
    @GetMapping("/getKnowledgePointsById/{id}")
    Map getKnowledgePointsById(@PathVariable("id") String id);


    /**
     * @author:胡立涛
     * @description: TODO 根据知识点id查询知识点分组及分组下的属性信息
     * @date: 2024/7/17
     * @param: [id]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping("/getProListByPointId/{id}")
    List<Map> getProListByPointId(@PathVariable String id) throws Exception;

    /**
     * @author:胡立涛
     * @description: TODO 获取知识点下的所有知识
     * @date: 2024/7/17
     * @param: [kpId]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping("/getAllKnowledgeListByPointId/{kpId}")
    List<Map> getAllKnowledgeListByPointId(@PathVariable("kpId") String kpId) throws Exception;

    /**
     * @author:胡立涛
     * @description: TODO 根据知识点id，属性id查询属性详细信息
     * @date: 2024/7/17
     * @param: [map]
     * @return: java.util.Map
     */
    @PostMapping(value = "/knowledgepoints/getProDetail")
    Map getProDetail(@RequestBody Map map) throws Exception;

    /**
     * @author:胡立涛
     * @description: TODO 根据知识id，属性id查询对应的属性值
     * @date: 2024/7/18
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getProValue")
    List<Map> getProValue(@RequestBody Map map) throws Exception;


    /**
     * @author:胡立涛
     * @description: TODO 推荐知识列表
     * @date: 2024/7/18
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/listKnowledgePage")
    List<Map> listKnowledgePage(@RequestBody Map map) throws Exception;

    /**
     * @author:胡立涛
     * @description: TODO 推荐知识列表总条数
     * @date: 2024/7/18
     * @param: [map]
     * @return: java.lang.Integer
     */
    @PostMapping(value = "/knowledgepoints/listKnowledgePageCount")
    Integer listKnowledgePageCount(@RequestBody Map map) throws Exception;

    /**
     * @author:胡立涛
     * @description: TODO 查询关系分组
     * @date: 2024/7/22
     * @param: [kpId]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping("/knowledgepoints/getRelationGroup/{kpId}")
    List<Map> getRelationGroup(@PathVariable("kpId") String kpId) throws Exception;


    /**
     * @author:胡立涛
     * @description: TODO 查询关系分组下的属性
     * @date: 2024/7/22
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getRelationPro")
    List<Map> getRelationPro(@RequestBody Map map) throws Exception;

    /**
     * @author:胡立涛
     * @description: TODO 根据关系id查询关系信息
     * @date: 2024/7/23
     * @param: [id]
     * @return: java.util.Map
     */
    @GetMapping("/knowledgepoints/getRelationGroupById/{id}")
    Map getRelationGroupById(@PathVariable("id") String id) throws Exception;

    /**
     * @author:胡立涛
     * @description: TODO 询关系中图片的值
     * @date: 2024/7/23
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getRelationProVal")
    List<Map> getRelationProVal(@RequestBody Map map) throws Exception;

    /**
     * @author:胡立涛
     * @description: TODO 关系：查询概念下的知识
     * @date: 2024/7/25
     * @param: [classId]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping("/knowledgepoints/getStepOne/{classId}")
    List<Map> getStepOne(@PathVariable("classId") String classId);

    /**
     * @author:胡立涛
     * @description: TODO 关系：查询概念下实际关联的知识
     * @date: 2024/7/25
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getStepTwo")
    List<Map> getStepTwo(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 关系：查询概念下知识下的属性值
     * @date: 2024/7/25
     * @param: [targetKnowledgeId]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping("/knowledgepoints/getStepThree/{targetKnowledgeId}")
    List<Map> getStepThree(@PathVariable("targetKnowledgeId") String targetKnowledgeId);

    /**
     * @author:胡立涛
     * @description: TODO 复杂知识模板：知识集：关系下的知识信息
     * @date: 2024/7/31
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getKnowledgModelStepOne")
    List<Map> getKnowledgModelStepOne(@RequestBody Map map) throws Exception;

    /**
     * @author:胡立涛
     * @description: TODO 根据属性code，属性值，知识点查询知识列表
     * @date: 2024/8/1
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getKnowledgeByProCode")
    List<Map> getKnowledgeByProCode(@RequestBody Map map);


    /**
     * @author:胡立涛
     * @description: TODO 根据知识点，生产国家查询知识数据
     * @date: 2024/8/2
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getRelationKnowledgesByParam")
    List<Map> getRelationKnowledgesByParam(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO deepseek 根据知识名称查询知识信息
     * @date: 2024/9/5
     * @param: [map]
     * @return: java.util.Map
     */
    @PostMapping(value = "/deepseek/deepseekKnowlege")
    Map deepseekKnowlege(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 查询知识的影像图片
     * @date: 2024/8/2
     * @param: [knowledgeId]
     * @return: java.util.Map
     */
    @GetMapping("/knowledgepoints/getTifImage/{knowledgeId}")
    Map getTifImage(@PathVariable("knowledgeId") String knowledgeId);

    /**
     * @author:胡立涛
     * @description: TODO 根据关系属性查询知识
     * @date: 2024/8/5
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getRelationProperty")
    List<Map> getRelationProperty(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 战斗舰艇判读所需对比图片查询
     * @date: 2024/8/6
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getPics")
    List<Map> getPics(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 根据多个知识点id查询知识点名称
     * @date: 2024/8/7
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getKnowledgePointsMapByIdList")
    List<Map> getKnowledgePointsMapByIdList(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 根据属性名称查询属性code值
     * @date: 2024/8/14
     * @param: [name]
     * @return: java.util.Map
     */
    @GetMapping("/knowledgepoints/getProCode/{name}")
    Map getProCode(@PathVariable("name") String name);

    /**
     * @author:胡立涛
     * @description: TODO 根据知识点名称查询知识点code
     * @date: 2024/8/14
     * @param: [name]
     * @return: java.util.Map
     */
    @GetMapping("/knowledgepoints/getPointCode/{name}")
    Map getPointCode(@PathVariable("name") String name);


    /**
     * @author:胡立涛
     * @description: TODO 查询舰载武器知识点列表
     * @date: 2024/8/15
     * @param: [name]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping("/knowledgepoints/getwqPoint/{name}")
    List<Map> getwqPoint(@PathVariable("name") String name);

    /**
     * @author:胡立涛
     * @description: TODO 根据知识点名称集合查询知识点信息
     * @date: 2024/8/16
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getPoints")
    List<Map> getPoints(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 舰载武器封面图片获取
     * @date: 2024/8/16
     * @param: [knowledgeId]
     * @return: java.util.Map
     */
    @GetMapping("/knowledgepoints/getPhotoImage/{knowledgeId}")
    Map getPhotoImage(@PathVariable("knowledgeId") String knowledgeId);


    /**
     * @author:胡立涛
     * @description: TODO 查询某知识的所有关系图片
     * @date: 2024/8/19
     * @param: [knowledgeId]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping("/knowledgepoints/getKnowledgePics/{knowledgeId}")
    List<Map> getKnowledgePics(@PathVariable("knowledgeId") String knowledgeId);

    /**
     * @author:胡立涛
     * @description: TODO 根据typeId查询枚举值列表
     * @date: 2024/8/21
     * @param: [typeId]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping("/knowledgepoints/getmjImage/{typeId}")
    List<Map> getmjImage(@PathVariable("typeId") String typeId);

    /**
     * @author:胡立涛
     * @description: TODO 舰面标识查询逻辑
     * @date: 2024/8/28
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/knowledgepoints/getxianhao")
    List<Map> getxianhao(@RequestBody Map map);


    /**
     * @author:胡立涛
     * @description: TODO 根据code查询系统字典
     * @date: 2024/8/29
     * @param: [code]
     * @return: java.util.Map
     */
    @GetMapping("/knowledgepoints/getSysDictionary/{code}")
    Map getSysDictionary(@PathVariable("code") String code);


    /**
     * @author:胡立涛
     * @description: TODO 将完成知识对接的知识点保存到xx_class表中
     * @date: 2024/9/20
     * @param: [map]
     * @return: void
     */
    @PostMapping(value = "/knowledgepoints/saveClass")
    void saveClass(@RequestBody Map map);
}


