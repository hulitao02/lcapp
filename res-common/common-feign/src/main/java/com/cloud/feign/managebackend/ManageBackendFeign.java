package com.cloud.feign.managebackend;

import com.cloud.model.common.Dict;
import com.cloud.model.common.IntDirect;
import com.cloud.model.common.KnowledgePoints;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;


import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author md
 * @date 2021/4/14 16:24
 */
@Component
@FeignClient(name = "manage-backend")
public interface ManageBackendFeign {

    @GetMapping("/getKnowledgePointsById/{id}")
    KnowledgePoints getKnowledgePointsById(@PathVariable("id") String id);

    @PostMapping("/getKnowledgePointsMapByIdList")
    Map<Long, String> getKnowledgePointsMapByIdList(@RequestBody List<String> idList);

    @GetMapping("/findIntDirectById/{id}")
    IntDirect findIntDirectById(@PathVariable("id") Long id);

    @PostMapping("/findIntDirectMap")
    Map<Long, String> findIntDirectMap(@RequestBody List<Long> idList);

    @GetMapping("/findDict")
    List<Dict> findDict(@RequestParam Map<String, Object> params);

    @GetMapping("getNameValueMap")
    Map<String, String> getNameValueMap(@RequestParam String type);

    @GetMapping("/getKnowledgePointNameMap")
    Map<String, String> getKnowledgePointNameMap();

    @GetMapping("/getKnowledgePointIdMap")
    Map<Long, String> getKnowledgePointIdMap();

    @PostMapping("/getKnowledgePointIdMapByIds")
    Map<Long, String> getKnowledgePointIdMapByIds(@RequestBody Collection<String> ids);

    @GetMapping("/knowledgepoints/all")
    List<KnowledgePoints> findAll();

    @GetMapping("/knowledgepoints/tree")
    List<KnowledgePoints> findKpTree();
    /**
     *
     * @author:胡立涛
     * @description: TODO 查询所有的知识点
     * @date: 2024/10/31
     * @param: []
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping("/knowledgepoints/alltwo")
    List<Map> alltwo();

    @PostMapping("/knowledgepoints/saveInfo")
    String saveInfo(KnowledgePoints knowledgePoints);

    @PostMapping(value = "/knowledgepoints/updateClass")
    String updateClass(KnowledgePoints knowledgePoints);

    @PostMapping(value = "/knowledgepoints/updateParentId")
    void updateParentId(KnowledgePoints knowledgePoints);

    @PostMapping(value = "/knowledgepoints/delClass")
    String delClass(KnowledgePoints knowledgePoints);

    @PostMapping(value = "/knowledgepoints/getClass")
    List<Map> getClass(KnowledgePoints knowledgePoints) throws Exception;

    @PostMapping(value = "/knowledgepoints/getClassById")
    List<Map> getClassById(@RequestParam KnowledgePoints knowledgePoints) throws Exception;


    /**
     * @author:胡立涛
     * @description: TODO 根据知识code查询知识点信息
     * @date: 2022/1/25
     * @param: [code]
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     */
    @GetMapping("/getKnowledgePointsByCode/{code}")
    Map getKnowledgePointsByCode(@PathVariable("code") String code);


    /**
     * @author:胡立涛
     * @description: TODO 根据知识code查询知识点信息
     * @date: 2022/1/25
     * @param: [code]
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     */
    @GetMapping(value = "/knowledgepoints/getKnowledgeBind/{ids}")
    List<Map> getKnowledgeBind(@PathVariable("ids") String ids);


    @GetMapping(value = "/knowledgepoints/getClassByIdGet/{rootId}")
    List<Map> getClassByIdGet(@PathVariable("rootId") Long rootId) throws Exception;

    /**
     * @author:胡立涛
     * @description: TODO 根据角色ids查询菜单列表
     * @date: 2022/3/15
     * @param: [roleIds]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping(value = "/menus/getMenuByRoleIds/{roleIds}")
    List<Map> getMenuByRoleIds(@PathVariable("roleIds") String roleIds);

    /**
     * @author:胡立涛
     * @description: TODO 查询根节点数据
     * @date: 2022/4/13
     * @param: []
     * @return: java.util.Map
     */
    @GetMapping(value = "/knowledgepoints/getRootPoint")
    Map getRootPoint();


    /**
     * @author: 胡立涛
     * @description: TODO 删除知识点
     * @date: 2022/5/28
     * @param: [kpIds]
     * @return: void
     */
    @PostMapping(value = "/knowledgepoints/delKnowledgePointManage")
    void delKnowledgePointManage(@RequestParam Map<String, Object> map);

    /**
     * @author: 胡立涛
     * @description: TODO 根据参数名称查询参数值
     * @date: 2022/5/30
     * @param: [paramName]
     * @return: java.lang.String
     */
    @PostMapping(value = "/knowledgeparam/getParamValue")
    String getParamValue(@RequestBody String paramName) throws Exception;

    /**
     * @author:胡立涛
     * @description: TODO 根据当前知识点，获取下一级知识点信息
     * @date: 2022/6/28
     * @param: [map]
     * @return: java.util.Map
     */
    @PostMapping(value = "/other/getNextPoint")
    List<Map> getNextPoint(@RequestBody Map<String, Object> map);


    /**
     *
     * @author:胡立涛
     * @description: TODO 根据知识点名称关键字，返回知识点ID的集合
     * @date: 2022/6/28
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "/other/getPointListForName")
    List<Map> getPointListForName(@RequestBody Map<String, Object> map);
}


