package com.cloud.model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.bean.vo.ModelDataVO;
import com.cloud.model.model.ModelData;

import java.util.List;
import java.util.Map;

public interface ModelDataService extends IService<ModelData> {


    boolean add(ModelData modelData);

    /**
     * 通过模板和知识点的ID 查询挂接组件信息
     *
     * @param modelData
     * @return
     */
    List<ModelDataVO> getAssemblyListByTemplate(ModelData modelData);

    /**
     *  通过知识点查询，当前与知识点挂接的所有的模型信息
     * @param modelData
     * @return
     */
    List<ModelDataVO> getModelKpIdListBykpId(ModelData modelData);

   /**
    *
    * @author:胡立涛
    * @description: TODO 完成数据挂接
    * @date: 2021/11/29
    * @param: [id]
    * @return: void
    */
    void finishData(Long id);

    void delPro(Map<String, Object> map);

}
