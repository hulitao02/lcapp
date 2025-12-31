package com.cloud.backend.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.model.common.KnowledgePoints;
import org.apache.ibatis.annotations.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Mapper
public interface KnowledgePointsDao extends BaseMapper<KnowledgePoints> {


    @Insert(" insert into knowledge_points(code,pointname, parentid ,describe, createtime,updatetime,creator,level,status)" +
            " values(#{code},#{pointName}, #{parentId}, #{describe}, #{createTime},#{updateTime},#{creator},#{level},#{status})")
    int save(KnowledgePoints knowledgePoints);

    @Insert(" insert into knowledge_points(id,code,pointname, parentid ,describe, createtime,updatetime,creator,level,status)" +
            " values(#{id},#{code},#{pointName}, #{parentId}, #{describe}, #{createTime},#{updateTime},#{creator},#{level},#{status})")
    int saveRoot(KnowledgePoints knowledgePoints);

    /**
     * @author:胡立涛
     * @description: TODO 知识点同步 新增
     * @date: 2022/2/18
     * @param: [knowledgePoints]
     * @return: int
     */
    @Insert(" insert into knowledge_points(pointname, parentid ,kp_label,status,code,parent_code,createtime,updatetime)" +
            " values(#{pointName}, #{parentId},#{kpLabel},#{status},#{code},#{parentCode},#{createTime},#{updateTime})")
    int savePoint(KnowledgePoints knowledgePoints);

    /**
     * @author:胡立涛
     * @description: TODO 知识点同步 更新
     * @date: 2022/2/18
     * @param: [knowledgePoints]
     * @return: int
     */
    @Update("update knowledge_points set pointName = #{pointName},parentid = #{parentId},kp_label = #{kpLabel},parent_code=#{parentCode},updatetime=#{updateTime}  where id = #{id}")
    int updatePoint(KnowledgePoints knowledgePoints);

    @Update("update knowledge_points set pointName =#{pointName},parentid= #{parentId} ,describe=#{describe}  where id = #{id}")
    int update(KnowledgePoints knowledgePoints);

    @Update("update knowledge_points set parentid= #{parentId}  where id = #{id}")
    int updateParentId(KnowledgePoints knowledgePoints);

    @Delete("delete from knowledge_points where id = #{id}")
    int delete(Long id);

    @Select("select * from knowledge_points t where t.id = #{id}")
    KnowledgePoints findById(Long id);

    @Select("select * from knowledge_points t where t.pointName = #{pointName}")
    KnowledgePoints findByName(String pointName);

    int count(Map<String, Object> params);

    List<KnowledgePoints> findData(Map<String, Object> params);

    @Select("select * from knowledge_points")
    List<KnowledgePoints> findAll();

    @Select("select parentId from knowledge_points")
    List<Long> parentIdList();

    List<Map> getClass(long parentId);
    List<Map> getClassDM(long parentId);

    List<Map> getClassById(long parentId);
    List<Map> getClassByIdDM(long parentId);

    /**
     * @author:胡立涛
     * @description: TODO 根据知识code查询知识信息
     * @date: 2022/1/25
     * @param: [code]
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     */
    Map getKnowledgePointsByCode(String code);


    /**
     * @author:胡立涛
     * @description: TODO 知识同步 添加知识点
     * @date: 2022/1/25
     * @param: [knowledgePoints]
     * @return: int
     */
    @Insert(" insert into knowledge_points(pointname, parentid ,code,parent_code, createtime,updatetime,creator,\"level\",status)" +
            " values(#{pointName}, #{parentId}, #{code},#{parentCode}, #{createTime},#{updateTime},#{creator},#{level},#{status})")
    int saveInfo(KnowledgePoints knowledgePoints);


    /**
     * @author:胡立涛
     * @description: TODO 获取绑定数据的知识点树形菜单
     * @date: 2022/3/14
     * @param: [pointIds]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getKnowledgeBind(@Param("idsList") List<Long> idsList);
    List<Map> getKnowledgeBindDM(@Param("idsList") List<Long> idsList);


    /**
     *
     * @author:胡立涛
     * @description: TODO 跟新根节点为非根节点
     * @date: 2022/4/13
     * @param: [code]
     * @return: void
     */
    @Update("UPDATE knowledge_points SET kp_label = 'no-root' WHERE kp_label = #{kpLabel}")
    void updateKpLabel(String kpLabel);

    /**
     *
     * @author:胡立涛
     * @description: TODO 查询根节点信息
     * @date: 2022/4/13
     * @param: [rootLabel]
     * @return: java.util.Map
     */
    @Select("select * from knowledge_points where kp_label=#{rootLabel}")
    Map getRootPoint(String rootLabel);


    /**
     *
     * @author:胡立涛
     * @description: TODO 查询需要更新的父节点id
     * @date: 2022/4/20
     * @param: []
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getUpdateInfo();


    /**
     *
     * @author: 胡立涛
     * @description: TODO 根据知识点id删除knowledgePoint信息
     * @date: 2022/5/28
     * @param: [kpIds]
     * @return: void
     */
    void delKnowledgePointManage(@Param("kpIds") Long[] kpIds);

    /**
     *
     * @author:胡立涛
     * @description: TODO 根据知识点查询该知识点的下一级知识点
     * @date: 2022/6/28
     * @param: [map]
     * @return: java.util.Map
     */
    List<Map> getNextPoint(@RequestBody Map<String, Object> map);


    /**
     *
     * @author:胡立涛
     * @description: TODO 根据知识点名称关键字，返回知识点ID的集合
     * @date: 2022/6/28
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getPointListForName(@RequestBody Map<String, Object> map);
}
