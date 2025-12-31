package com.cloud.model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.bean.vo.StudyPlanVo;
import com.cloud.model.dao.StudyPlanDao;
import com.cloud.model.dao.StudyTimeDao;
import com.cloud.model.dao.StudyTimeStatisticsDao;
import com.cloud.model.model.StudyKnowledge;
import com.cloud.model.model.StudyPlan;
import com.cloud.model.model.StudyTime;
import com.cloud.model.model.StudyTimeStatistics;
import com.cloud.model.service.StudyKnowledgeService;
import com.cloud.model.service.StudyPlanService;
import com.cloud.model.service.StudyTimeService;
import com.cloud.model.user.AppUser;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Transient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class StudyTimeServiceImpl extends ServiceImpl<StudyTimeDao, StudyTime>
        implements StudyTimeService {

    @Autowired
    StudyTimeDao studyTimeDao;
    @Autowired
    StudyTimeStatisticsDao studyTimeStatisticsDao;
    @Autowired
    SysDepartmentFeign sysDepartmentFeign;

    // 数据库标识 1：达梦数据库 2：pg数据库 3:人大金仓
    @Value(value = "${db-type}")
    private Integer dbType;


    /**
     * @author:胡立涛
     * @description: TODO 添加学习
     * @date: 2022/4/11
     * @param: [studyTime, timeMinute]
     * @return: void
     */
    @Override
    @Transient
    public void saveInfo(Map<String,Object> map) throws Exception {
        // 计算数据库表名称
        SimpleDateFormat curDate = new SimpleDateFormat("yyyyMM");
        String curDateStr = curDate.format(new Date());
        String tableName = "study_time_" + curDateStr;
        map.put("tableName",tableName);
        // 验证该表是否存在
//        int table = studyTimeDao.getTable(tableName);
//      默认PG的
        String dbTableName = "pg_tables";
        String fileName = "tablename";
        if(dbType.intValue() == 3){
            dbTableName = "dba_tables";
            fileName = "Table_Name";
        }else if (dbType.intValue() == 1){
            dbTableName = "dba_tables";
            fileName = "Table_Name";
        }
        map.put("fileName",fileName);
        map.put("dbTableName",dbTableName);
        int table = this.studyTimeDao.getTableCountByDbType(map);
        if (table == 0) {
            // 创建表
//            studyTimeDao.createTable(map);
//            TODO 新增加表创建，数据库不同，创建SQL不同 。
            createKingBaseTable(dbType,tableName,map);

        }
        // 添加数据
        map.put("startTime",Timestamp.valueOf(map.get("startTime").toString()));
        map.put("createTime",new Timestamp(System.currentTimeMillis()));

        studyTimeDao.saveInfo(map);
    }

    /**
     *
     * @param tableName
     */
    @Override
    @Transient
    public void  createKingBaseTable(Integer dbType,String tableName,Map<String,Object> map){

        String tableSequence = tableName+"_sq_1";
        map.put("squence",tableSequence);
        if(dbType.intValue() == 3){
            studyTimeDao.createSequenceKingBase(map);
            studyTimeDao.createTableKingBase(map);
        }else if(dbType.intValue() == 2){
            map.put("primary",tableName+"_pkey");
            studyTimeDao.createPgTable(map);
        }else{
            studyTimeDao.createTable(map);
        }
    }





    /**
     *
     * @author:胡立涛
     * @description: TODO 查询所有用户的学习时间
     * @date: 2022/4/11
     * @param: [parMap]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    @Override
    @Transient
    public void calculationStudyTime(Map<String, Object> parMap) throws Exception {
        studyTimeStatisticsDao.delInfo(parMap);
        // 统计当前数据
        List<Map<String, Object>> dayInfoList = studyTimeDao.getDayInfo(parMap);
        if (dayInfoList != null && dayInfoList.size() > 0) {
            for (Map<String, Object> map : dayInfoList) {
                // 将统计数据保存至统计表中
                StudyTimeStatistics studyTimeStatistics = new StudyTimeStatistics();
                studyTimeStatistics.setKnId(map.get("kn_id").toString());
                studyTimeStatistics.setUserId(Integer.parseInt(map.get("user_id").toString()));
                studyTimeStatistics.setTimeCount(Integer.valueOf(map.get("sum").toString()));
                studyTimeStatistics.setTimeDay(parMap.get("timeDay").toString());
                studyTimeStatistics.setCreateTime(new Timestamp(System.currentTimeMillis()));
                studyTimeStatisticsDao.insert(studyTimeStatistics);
            }
        }
    }
}
