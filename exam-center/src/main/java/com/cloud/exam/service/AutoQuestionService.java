package com.cloud.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.exam.model.exam.Question;
import java.util.Map;


public interface AutoQuestionService extends IService<Question> {


    /**
     *
     * @author:胡立涛
     * @description: TODO 自动出题 单选 模板一
     * @date: 2022/11/10
     * @param: [map]
     * @return: void
     */
    int dxTypeOne(Map<String,Object> map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 自动出题 单选 模板二
     * @date: 2022/11/11
     * @param: [map]
     * @return: int
     */
    int dxTypeTwo(Map<String,Object> map);


    /**
     *
     * @author:胡立涛
     * @description: TODO 自动出题 判断题
     * @date: 2022/11/14
     * @param: [map]
     * @return: int
     */
    int pd(Map<String,Object> map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 自动出题 填空题
     * @date: 2022/11/14
     * @param: [map]
     * @return: int
     */
    int tk(Map<String,Object> map);


    /**
     *
     * @author:胡立涛
     * @description: TODO 自动出题 多选
     * @date: 2022/11/15
     * @param: [map]
     * @return: int
     */
    int duoxuan(Map<String,Object> map);

}
