package com.cloud.model.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.model.model.Image;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface ImageDao extends BaseMapper<Image> {
}
