package com.cloud.model.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.model.model.StudyTimeStatistics;
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
public interface StudyTimeStatisticsDao extends BaseMapper<StudyTimeStatistics> {

    /**
     *
     * @author:胡立涛
     * @description: TODO 根据用户id，日期查询数据是否存在
     * @date: 2022/4/11
     * @param: [map]
     * @return: int
     */
    int getInfo(Map<String,Object> map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 根据用户id，日期删除数据
     * @date: 2022/4/11
     * @param: [map]
     * @return: void
     */
    void delInfo(Map<String,Object> map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 电子所：统计所有用户的学习时长
     * @date: 2022/4/11
     * @param: []
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String,Object>> getTotalInfo();


    List<Map<String,Object>> getPerStudyHoursAndRank(String timeDay);
}
