package com.cloud.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.common.Page;
import com.cloud.model.user.SysDepartment;

import java.util.List;
import java.util.Map;

public interface SysDepartmentService extends IService<SysDepartment> {
    int add(SysDepartment sysDepartment);

    int update(SysDepartment sysDepartment);

    int delete(Long id);

    SysDepartment findById(Long id);

    SysDepartment findByName(String name);

    Page<SysDepartment> findByPage(Map<String, Object> params);

    List<SysDepartment> findAll();

}
