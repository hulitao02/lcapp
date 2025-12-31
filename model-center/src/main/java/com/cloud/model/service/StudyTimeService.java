package com.cloud.model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.model.StudyTime;
import java.util.Map;

public interface StudyTimeService extends IService<StudyTime> {


    public void  createKingBaseTable(Integer dbType,String tableName,Map<String,Object> map);

    void saveInfo(Map<String,Object> map)throws Exception;

    void calculationStudyTime(Map<String,Object> map) throws Exception;
}
