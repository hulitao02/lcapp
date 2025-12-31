package com.cloud.model.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.model.FocusKp;

public interface FocusKpService extends IService<FocusKp> {
    IPage<FocusKp> selectFocusListForPage(String searchParam, Integer userId, Integer pageNo, Integer pageSize);
}
