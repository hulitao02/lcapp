package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.exam.model.exam.ManageGroup;
import com.cloud.exam.model.exam.PgInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;


@Mapper
public interface PgInfoDao extends BaseMapper<PgInfo> {

    /**
     * @author:胡立涛
     * @description: TODO 本次考试没人拥有的知识点、判读类型
     * @date: 2024/6/24
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> baseInfo(Map map);

    /**
     * @author:胡立涛
     * @description: TODO A部分结果计算
     * @date: 2024/6/24
     * @param: [map]
     * @return: java.util.Map
     */
    Map getA(Map map);

    /**
     * @author:胡立涛
     * @description: TODO B部分结果计算
     * @date: 2024/6/24
     * @param: [map]
     * @return: java.util.Map
     */
    Map getB(Map map);

    /**
     * @author:胡立涛
     * @description: TODO 个人能力评估：能力分布
     * @date: 2024/6/25
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> ability(Map map);

    List<Map> khNum(Map map);

    /**
     * @author:胡立涛
     * @description: TODO 个人能力评估：能力趋势
     * @date: 2024/6/25
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> history(Map map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 查询登录人所在部门的人员
     * @date: 2024/6/26
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> deptUser(Map map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 个人能力评估：查询默认知识点
     * @date: 2024/6/26
     * @param: [map]
     * @return: java.util.Map
     */
    Map getKpId(Map map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 团体能力评估：单位综合能力
     * @date: 2024/6/26
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> kpScore(Map map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 团体能力评估：成员能力排名
     * @date: 2024/6/27
     * @param: [map]
     * @return: java.util.Map
     */
    Map deptAbility(Map map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 团体能力评估：成员能力分布
     * @date: 2024/6/27
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> distribution(Map map);

}
