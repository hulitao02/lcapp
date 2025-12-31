package com.cloud.model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.model.ModelControl;

import java.util.Map;

public interface ModelControlService extends IService<ModelControl> {

    /**
     * @author:胡立涛
     * @description: TODO 复杂模板修改子项顺序
     * @date: 2022/9/22
     * @param: [map]
     * @return: void
     */
    Integer saveInfoList(Map<String, Object> map);

    /**
     * @author:胡立涛
     * @description: TODO 删除子项信息
     * @date: 2022/9/26
     * @param: [modelControl]
     * @return: void
     */
    void delModelControl(ModelControl modelControl);
}
