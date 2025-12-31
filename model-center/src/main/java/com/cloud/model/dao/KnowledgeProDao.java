package com.cloud.model.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.model.bean.vo.KnowledgeProVO;
import com.cloud.model.model.KnowledgePro;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface KnowledgeProDao extends BaseMapper<KnowledgePro> {

    List<KnowledgeProVO> getKpProList(Map<String, Object> paramMap);

    /**
     * @author:胡立涛
     * @description: TODO 修改属性分组名称
     * @date: 2022/1/25
     * @param: [map]
     * @return: void
     */
    void updateInfoByTypeCode(Map map);

    /**
     * @author:胡立涛
     * @description: TODO 修改属性名称
     * @date: 2022/1/25
     * @param: [map]
     * @return: void
     */
    void updateInfoByProCode(Map map);

    /**
     * @author:胡立涛
     * @description: TODO 删除属性
     * @date: 2022/1/25
     * @param: [proCode]
     * @return: void
     */
    void delInfoByProCode(String proCode);


    /**
     * @author:胡立涛
     * @description: TODO 删除属性分组
     * @date: 2022/1/25
     * @param: [proTypeCode]
     * @return: void
     */
    void delInfoByProTypeCode(String proTypeCode);


    /**
     * @author:胡立涛
     * @description: TODO 根据属性code，知识点id删除对应数据
     * @date: 2022/8/8
     * @param: [map]
     * @return: void
     */
    void delInfoByProCodeAndKpId(Map<String, Object> map);


}
