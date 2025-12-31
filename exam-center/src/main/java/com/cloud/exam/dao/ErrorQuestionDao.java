package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.exam.model.exam.ErrorQuestion;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author:胡立涛
 * @description: TODO 错误试题
 * @date: 2022/8/18
 * @param:
 * @return:
 */
@Mapper
public interface ErrorQuestionDao extends BaseMapper<ErrorQuestion> {


    /**
     * @author:胡立涛
     * @description: TODO 根据参数查询错误试题信息
     * @date: 2022/8/19
     * @param: [map]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    List<Map<String, Object>> findByPar(Map<String, Object> map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 理论自测时，统计本次训练错误试题列表
     * @date: 2022/8/19
     * @param: [map]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String,Object>> errorList(Map<String,Object> map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 保存错误试题
     * @date: 2022/8/19
     * @param: [map]
     * @return: void
     */
    void saveInfo(Map<String,Object> map);


    /**
     *
     * @author:胡立涛
     * @description: TODO 更新错误试题的创建时间
     * @date: 2022/8/22
     * @param: [map]
     * @return: void
     */
    void updateInfo(Map<String,Object> map);


}
