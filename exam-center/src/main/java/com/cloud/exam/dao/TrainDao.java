package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.exam.model.train.Train;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface TrainDao extends BaseMapper<Train> {


}
