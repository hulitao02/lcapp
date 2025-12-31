package com.cloud.knowledge.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.knowledge.model.OntologyClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;


@Mapper
public interface KnowledgeDao extends BaseMapper<OntologyClass> {


    /**
     * @author:胡立涛
     * @description: TODO 获取根节点下的所有知识点 label为root
     * @date: 2024/7/16
     * @param: []
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getTree();

    /**
     *
     * @author:胡立涛
     * @description: TODO 查询封面图
     * @date: 2024/8/14
     * @param: [map]
     * @return: java.util.Map
     */
    Map getProByPar (@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 查询已完成数据对接的知识点
     * @date: 2024/7/17
     * @param: [idsList]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getKnowledgeBind(@Param("idsList") List<String> idsList);

    /**
     * @author:胡立涛
     * @description: TODO 获取根节点信息
     * @date: 2024/7/17
     * @param: []
     * @return: java.util.Map
     */
    Map getRootPoint();

    /**
     * id
     *
     * @author:胡立涛
     * @description: TODO 根据知识点id查询知识点信息
     * @date: 2024/7/17
     * @param: [id]
     * @return: java.util.Map
     */
    Map getKnowledgePointsById(@PathVariable("id") String id);

    /**
     * @author:胡立涛
     * @description: TODO 根据知识点查询属性分组
     * @date: 2024/7/17
     * @param: [kpId]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getKpGroup(@PathVariable("kpId") String kpId);

    /**
     * @author:胡立涛
     * @description: TODO 查询知识点分组下的属性信息
     * @date: 2024/7/17
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getGroupPro(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 获取知识点下的所有知识
     * @date: 2024/7/17
     * @param: [kpId]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getAllKnowledgeListByPointId(@PathVariable("kpId") String kpId);

    /**
     * @author:胡立涛
     * @description: TODO 根据id和知识点查询属性或分组详细信息
     * @date: 2024/7/17
     * @param: [map]
     * @return: java.util.Map
     */
    Map getProDetail(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 根据知识id，属性id查询属性值
     * @date: 2024/7/18
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getProValue(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 知识推荐列表
     * @date: 2024/7/18
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> listKnowledgePage(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 知识推荐列表总条数
     * @date: 2024/7/18
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    Map listKnowledgePageCount(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 关系分组
     * @date: 2024/7/22
     * @param: [kpId]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getRelationGroup(@PathVariable("kpId") String kpId);

    /**
     * @author:胡立涛
     * @description: TODO 关系分组下的属性
     * @date: 2024/7/22
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getRelationPro(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 根据关系id查询关系信息
     * @date: 2024/7/23
     * @param: [id]
     * @return: java.util.Map
     */
    Map getRelationGroupById(@PathVariable("id") String id);

    /**
     * @author:胡立涛
     * @description: TODO 关系图片值获取
     * @date: 2024/7/23
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getRelationProVal(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 关系：查询概念下的知识
     * @date: 2024/7/25
     * @param: [classId]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getStepOne(@PathVariable("classId") String classId);

    /**
     * @author:胡立涛
     * @description: TODO 关系：查询概念下实际关联的知识
     * @date: 2024/7/25
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getStepTwo(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 关系：查询概念下知识下的属性值
     * @date: 2024/7/25
     * @param: [targetKnowledgeId]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getStepThree(@PathVariable("targetKnowledgeId") String targetKnowledgeId);

    /**
     * @author:胡立涛
     * @description: TODO 复杂知识模板：知识集：关系下的知识信息
     * @date: 2024/7/31
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getKnowledgModelStepOne(@RequestBody Map map);


    /**
     * @author:胡立涛
     * @description: TODO 根据属性值查询知识
     * @date: 2024/8/1
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getKnowledgeByProCode(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 根据知识点，生产国家查询知识数据
     * @date: 2024/8/2
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getRelationKnowledgesByParam(@RequestBody Map map);


    /**
     * @author:胡立涛
     * @description: TODO 查询知识的影像图片
     * @date: 2024/8/2
     * @param: [knowledgeId]
     * @return: java.util.Map
     */
    Map getTifImage(@PathVariable("knowledgeId") String knowledgeId);

    /**
     * @author:胡立涛
     * @description: TODO 舰载武器封面图片获取
     * @date: 2024/8/16
     * @param: [knowledgeId]
     * @return: java.util.Map
     */
    Map getPhotoImage(@PathVariable("knowledgeId") String knowledgeId);

    /**
     * @author:胡立涛
     * @description: TODO 根据关系属性查询知识
     * @date: 2024/8/5
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getRelationProperty(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 战斗舰艇判读所需对比图片查询
     * @date: 2024/8/6
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getPics(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 根据多个知识点id查询知识点名称
     * @date: 2024/8/7
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getKnowledgePointsMapByIdList(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 根据属性名称查询属性code
     * @date: 2024/8/14
     * @param: [name]
     * @return: java.util.Map
     */
    Map getProCode(@PathVariable("name") String name);

    /**
     * @author:胡立涛
     * @description: TODO 根据知识点名称查询知识点code
     * @date: 2024/8/14
     * @param: [name]
     * @return: java.util.Map
     */
    Map getPointCode(@PathVariable("name") String name);

    /**
     * @author:胡立涛
     * @description: TODO 查询舰载武器知识点列表
     * @date: 2024/8/15
     * @param: [name]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getwqPoint(@PathVariable("name") String name);

    /**
     * @author:胡立涛
     * @description: TODO 根据知识点名称集合查询知识点信息
     * @date: 2024/8/16
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getPoints(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 查询某知识的所有关系图片
     * @date: 2024/8/19
     * @param: [knowledgeId]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getKnowledgePics(@PathVariable("knowledgeId") String knowledgeId);

    /**
     * @author:胡立涛
     * @description: TODO 根据typeId查询枚举值列表
     * @date: 2024/8/21
     * @param: [knowledgeId]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getmjImage(@PathVariable("typeId") String typeId);

    /**
     * @author:胡立涛
     * @description: TODO 舰面标识查询逻辑
     * @date: 2024/8/28
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getxianhao(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 根据code查询系统字典
     * @date: 2024/8/29
     * @param: [code]
     * @return: java.util.Map
     */
    Map getSysDictionary(@PathVariable("code") String code);

    /**
     * @author:胡立涛
     * @description: TODO 根据知识名称查询知识信息
     * @date: 2025/1/24
     * @param: [map]
     * @return: java.util.Map
     */
    Map getKnowledgeByName(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 添加图片知识实体信息
     * @date: 2025/1/24
     * @param: [map]
     * @return: void
     */
    Map save_ontology_individual(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 添加图片与关系之间的关系（如：高波级通用驱逐舰01与图片关系）
     * @date: 2025/1/24
     * @param: [map]
     * @return: void
     */
    void save_ontology_class_individual(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 添加图片实体中图片名称属性值
     * @date: 2025/1/24
     * @param: [map]
     * @return: void
     */
    void save_ontology_individual_dp_name(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 添加图片实体中图片地址属性值
     * @date: 2025/1/24
     * @param: [map]
     * @return: void
     */
    void save_ontology_individual_dp_pic_path(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 添加图片实体中图片拍摄位置属性值
     * @date: 2025/1/24
     * @param: [map]
     * @return: void
     */
    void save_ontology_individual_dp_pswz(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 添加图片实体中图片拍摄角度属性值
     * @date: 2025/1/24
     * @param: [map]
     * @return: void
     */
    void save_ontology_individual_dp_psjd(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 拍摄位置，拍摄角度查询
     * @date: 2025/1/24
     * @param: [map]
     * @return: java.util.Map
     */
    Map get_sys_dictionary(@RequestBody Map map);

    /**
     * @author:胡立涛
     * @description: TODO 根据知识点名称查询知识点信息
     * @date: 2025/1/24
     * @param: [map]
     * @return: java.util.Map
     */
    Map get_ontology_class(@RequestBody Map map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 添加知识与图片关系（高波级通用驱逐舰-常规图片集合-高波级通用驱逐舰01）
     * @date: 2025/1/24
     * @param: [map]
     * @return: void
     */
    void save_ontology_individual_op(@RequestBody Map map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 查询知识下的方位图片知识id
     * @date: 2025/2/13
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> dj_getpicknowledge(@RequestBody Map map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 查询知识信息
     * @date: 2025/2/13
     * @param: [map]
     * @return: java.util.Map
     */
    Map dj_knowledge(@RequestBody Map map);

    /**
     *
     * @author:胡立涛
     * @description: TODO deepseek 根据知识名称查询知识信息
     * @date: 2024/9/5
     * @param: [map]
     * @return: java.util.Map
     */
    Map deepseekKnowlege(@RequestBody Map map);

}
