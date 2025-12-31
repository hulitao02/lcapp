package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.exam.model.course.CourseUserRel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by dyl on 2021/03/22.
 */
@Mapper
public interface CourseUserRelDao extends BaseMapper<CourseUserRel> {

    List<Map<String,Integer>> getPerMonthCountByUser(@Param("paramsDate") String paramsDate, @Param("userId") Long userId);

    /**
     *
     * @author:胡立涛
     * @description: TODO 课后练习 错题收录
     * @date: 2022/8/24
     * @param: [map]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> errorList(Map<String,Object> map);
}
