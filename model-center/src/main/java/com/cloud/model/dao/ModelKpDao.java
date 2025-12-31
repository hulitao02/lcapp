package com.cloud.model.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.model.bean.vo.KnowledgePointVo;
import com.cloud.model.bean.vo.ModelKpVo;
import com.cloud.model.model.ModelKp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ModelKpDao extends BaseMapper<ModelKp> {

    ModelKpVo getModelKpVoById(@Param("modelKpId") Long modelKpId);

    List<ModelKpVo> getVolListByKpId(@Param("kpId") String kpId);

    IPage<ModelKpVo> getVolListByKpId(Page page, @Param("kpId") String kpId);

    List<KnowledgePointVo> getModelCount(Map<String, Object> params);

    /**
     *
     * @author: 胡立涛
     * @description: TODO 查询完成数据对接的知识点
     * @date: 2022/5/30
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getKpList(Map<String,Object> map);


    /**
     *
     * @author:胡立涛
     * @description: TODO 查询某知识点下的所有模板
     * @date: 2022/6/28
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    List<Map> getCount(Map<String,Object> map);


}
