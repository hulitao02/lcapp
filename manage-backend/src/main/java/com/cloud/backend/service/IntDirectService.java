package com.cloud.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.backend.model.CountryDict;
import com.cloud.model.common.IntDirect;
import com.cloud.model.common.Page;

import java.util.List;
import java.util.Map;

public interface IntDirectService extends IService<IntDirect> {

    int update(IntDirect intDirect);

    int delete(Long id);

    IntDirect findById(Long id);

    IntDirect findByName(String name);

    Page<IntDirect> findByPage(Map<String, Object> params);

    List<IntDirect> findDirectCountryAll();

    List<CountryDict> findAll();

}
