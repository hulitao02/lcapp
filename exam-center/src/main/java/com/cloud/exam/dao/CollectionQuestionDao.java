package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.exam.model.exam.CollectionQuestion;
import com.cloud.exam.model.exam.UserActivityMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author:胡立涛
 * @description: TODO 试题收藏
 * @date: 2022/8/18
 * @param:
 * @return:
 */
@Mapper
public interface CollectionQuestionDao extends BaseMapper<CollectionQuestion> {
    /**
     * @author:胡立涛
     * @description: TODO 收藏试题信息
     * @date: 2022/8/18
     * @param: [map]
     * @return: void
     */
    void saveInfo(Map<String, Object> map);

    /**
     * @author:胡立涛
     * @description: TODO 取消收藏试题
     * @date: 2022/8/18
     * @param: [map]
     * @return: void
     */
    void delInfo(Map<String, Object> map);

    /**
     * @author:胡立涛
     * @description: TODO 根据参数查询收藏试题信息
     * @date: 2022/8/19
     * @param: [map]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    List<Map<String, Object>> findByPar(Map<String, Object> map);


}
