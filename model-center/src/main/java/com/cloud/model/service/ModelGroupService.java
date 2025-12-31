package com.cloud.model.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.model.ModelGroup;

import java.util.List;

public interface ModelGroupService extends IService<ModelGroup> {
    Boolean add(String name);

    Boolean updateGroup(ModelGroup modelGroup);

    void deleteGroup(Long id);

    List<ModelGroup> queryAll();

    ModelGroup queryById(Long id);
}
