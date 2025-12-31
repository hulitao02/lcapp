package com.cloud.model.service;

import java.util.List;
import java.util.Map;

public interface DataService {

    void delProName(Map<String, Object> map);

    void delProTypeName(Map<String, Object> map);


    /**
     * @author:胡立涛
     * @description: TODO 删除知识点及知识点下的关联关系
     * @date: 2021/12/9
     * @param: [list]
     * @return: void
     */
    void delClass(List<Map> list);

    /**
     * @author:胡立涛
     * @description: TODO 批量 知识点属性数据同步
     * @date: 2022/2/18
     * @param: [list]
     * @return: void
     */
    void bathPro(List<Map> list);

    /**
     * @author:胡立涛
     * @description: TODO 批量 知识点关系数据同步
     * @date: 2022/2/18
     * @param: [list]
     * @return: void
     */
    void bathRelation(List<Map> list);

    /**
     *
     * @author:胡立涛
     * @description: TODO 根据知识点id，属性code删除数据
     * @date: 2022/8/8
     * @param: [map]
     * @return: void
     */
    void delProNameAndKpCode(Map<String, Object> map);
}
