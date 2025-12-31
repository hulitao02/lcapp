package com.cloud.model.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.cloud.model.bean.dto.QuestionDto;
import com.cloud.model.model.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


@Mapper
public interface QuestionDao extends BaseMapper<Question> {

    /**
     * 自定义分页查询
     *
     * @return
     * @Param("ew") Wrapper<T> queryWrapper
     */
    IPage<QuestionDto> getQuestionListPage(IPage<QuestionDto> pageParams, @Param(Constants.WRAPPER) Wrapper wrapper);

    /**
     * @author:胡立涛
     * @description: TODO 查询专家回答信息
     * @date: 2022/1/20
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    Map getAnswer(Map map);


    /**
     * @author: 胡立涛
     * @description: TODO 根据知识code查询question信息
     * @date: 2022/5/28
     * @param: [kIds]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getQuestion(@Param("kIds") String[] kIds);

    /**
     * @author: 胡立涛
     * @description: TODO 根据知识code删除question信息
     * @date: 2022/5/28
     * @param: [kIds]
     * @return: void
     */
    void delQuestion(@Param("kIds") String[] kIds);

    /**
     * @author: 胡立涛
     * @description: TODO 根据问题id删除answer信息
     * @date: 2022/5/28
     * @param: [qIds]
     * @return: void
     */
    void delAnswer(@Param("qIds") Long[] qIds);


}
