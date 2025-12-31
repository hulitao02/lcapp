package com.cloud.model.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.model.bean.vo.PageModelVo;
import com.cloud.model.model.PageModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface PageModelDao extends BaseMapper<PageModel> {
    IPage<PageModelVo> getVoListByGroupId(Page page, @Param("groupId") Long groupId);
}
