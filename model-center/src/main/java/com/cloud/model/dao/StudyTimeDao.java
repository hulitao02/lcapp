package com.cloud.model.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.model.model.StudyTime;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;


/**
 *
 * @author:胡立涛
 * @description: TODO 学习时长业务逻辑
 * @date: 2022/4/11
 * @param:
 * @return:
 */
@Mapper
public interface StudyTimeDao extends BaseMapper<StudyTime> {

    /**
     *
     * @author:胡立涛
     * @description: TODO 查看表是否存在
     * @date: 2022/4/11
     * @param: [tableName]
     * @return: int
     */
    int getTable(String tableName);


    /**
     *   传递类型不同 查询SQL不同
     * @param map
     * @return
     */
    public int getTableCountByDbType(Map<String,Object> map);


    /**
     *
     * @author:胡立涛
     * @description: TODO 创建表
     * @date: 2022/4/11
     * @param:
     * @return:
     */
    void createTable(Map<String,Object> map);

    /**
     *  创建 PG 数据库表，设置主键
     * @param map
     */
    public void createPgTable(Map<String,Object> map);

    /**
     * 人大金仓数据库 创建表
     * @param map
     */
    void createTableKingBase(Map<String,Object> map);

    /**
     *  R3 只支持手动创建序列 ，ID自增
     * @param map
     */
    void createSequenceKingBase(Map<String,Object> map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 添加学习时间数据
     * @date: 2022/4/11
     * @param: [map]
     * @return: void
     */
    void saveInfo(Map<String,Object> map);


    /**
     *
     * @author:胡立涛
     * @description: TODO 根据用户，知识查询最新数据
     * @date: 2022/4/11
     * @param: [map]
     * @return: java.util.Map<java.lang.String,java.lang.Object>
     */
    Map<String,Object> getInfo(Map<String,Object> map);


    /**
     *
     * @author:胡立涛
     * @description: TODO 根据id更新学习时间信息
     * @date: 2022/4/11
     * @param: [map]
     * @return: void
     */
    void updateInfo(Map<String,Object> map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 统计当天的学习数据
     * @date: 2022/4/11
     * @param: [map]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String,Object>> getDayInfo(Map<String,Object> map);
}
