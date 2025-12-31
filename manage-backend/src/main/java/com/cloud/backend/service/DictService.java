package com.cloud.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.common.Dict;
import com.cloud.model.common.Page;

import java.util.List;
import java.util.Map;

public interface DictService extends IService<Dict> {
     void saveDict(Dict dict);

     void update(Dict dict);

     void delete(Long id);

     Page<Dict> findPermissions(Map<String, Object> params);

     List<Dict> findDict(Map<String, Object> params);

     String getThemeType();

     void setThemeType(Long id);

     List<Dict> getThemeTypeList(Map<String, Object> params);

    Dict getLogoDict(String logoType, String key);
}
