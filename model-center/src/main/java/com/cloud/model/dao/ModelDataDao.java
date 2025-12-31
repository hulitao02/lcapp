package com.cloud.model.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.model.bean.vo.ModelDataVO;
import com.cloud.model.model.ModelData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ModelDataDao extends BaseMapper<ModelData> {

    List<ModelDataVO> getAssemblyListByTemplate(ModelData modelData);

    List<ModelDataVO> getModelKpIdListBykpId(ModelData modelData);

    List<Map> getModelDataByParam(Map<String,Object> map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 根据 model_kp_id 查询组件类型
     * @date: 2022/1/14
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getAssemblyName(Map<String,Object> map);

    /**
     *
     * @author:胡立涛
     * @description: TODO 根据 model_kp_id，assembly_name 查询信息
     * @date: 2022/1/14
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getProInfoList(Map<String,Object> map);


    /**
     *
     * @author:胡立涛
     * @description: TODO 删除属性
     * @date: 2022/1/25
     * @param: [proCode]
     * @return: void
     */
    void delInfoByProCode(String proCode);

    /**
     *
     * @author:胡立涛
     * @description: TODO 删除属性分组
     * @date: 2022/1/25
     * @param: [proTypeCode]
     * @return: void
     */
    void delInfoByProTypeCode(String proTypeCode);


    /**
     *
     * @author:胡立涛
     * @description: TODO 根据属性code，知识点id删除数据
     * @date: 2022/8/8
     * @param: [map]
     * @return: void
     */
    void delInfoByProCodeAndKpId(Map<String,Object> map);


    /**
     *
     * @author:胡立涛
     * @description: TODO 根据关系code查询信息
     * @date: 2022/8/9
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getRelationByProTypecode(Map<String,Object> map);


    /**
     *
     * @author:胡立涛
     * @description: TODO 查询知识关系数据的属性信息
     * @date: 2022/9/21
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getRelationKnowledgeAssemblyList(Map<String,Object> map);


}
