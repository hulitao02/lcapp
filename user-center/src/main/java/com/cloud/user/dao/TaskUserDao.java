package com.cloud.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.user.model.LcTaskUser;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface TaskUserDao extends BaseMapper<LcTaskUser> {

}
