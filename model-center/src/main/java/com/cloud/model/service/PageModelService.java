package com.cloud.model.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.bean.vo.PageModelVo;
import com.cloud.model.model.PageModel;

public interface PageModelService extends IService<PageModel> {

    IPage<PageModelVo> listByPage(Long groupId, Integer pageNum, Integer pageSize);

    Boolean deleteById(Long id);
}
