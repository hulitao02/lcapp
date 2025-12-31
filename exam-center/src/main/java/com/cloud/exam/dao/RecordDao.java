package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.cloud.core.ApiResult;
import com.cloud.exam.model.record.Record;
import com.cloud.exam.model.record.RecordDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface RecordDao extends BaseMapper<Record> {

    public ApiResult saveRecordInfos(RecordDto recordDto);
    /**
     *  自定义 查询
     * @param page
     * @param wrapper
     * @return
     */
    public IPage<RecordDto> getRecordPageInfo(IPage<RecordDto> page, @Param(Constants.WRAPPER) Wrapper wrapper);



}
