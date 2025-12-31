package com.cloud.model.schedule;

import com.cloud.model.dao.StudyTimeDao;
import com.cloud.model.dao.StudyTimeStatisticsDao;
import com.cloud.model.model.StudyTimeStatistics;
import com.cloud.model.service.StudyTimeService;
import com.cloud.model.user.AppUser;
import com.cloud.model.user.SysDepartment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class UpdateStudyTimeStatistics {

    @Autowired
    StudyTimeService studyTimeService;
    @Autowired
    StudyTimeStatisticsDao studyTimeStatisticsDao;
    @Autowired
    StudyTimeDao studyTimeDao;

    // 每天23点执行
    @Scheduled(cron = "0 0 23 * * ?")
    //@Scheduled(cron = "*/5 * * * * ?")
    public void startActivity() throws Exception {
        System.out.println("-----定时任务执行");
        Map<String, Object> parMap = new HashMap<>();
        // 数据库表名称计算
        SimpleDateFormat curDate = new SimpleDateFormat("yyyyMM");
        String curDateStr = curDate.format(new Date());
        String tableName = "study_time_" + curDateStr;
        int table = studyTimeDao.getTable(tableName);
        if (table>0){
            curDate = new SimpleDateFormat("yyyyMMdd");
            String timeDay = curDate.format(new Date());
            // 根据日期删除统计表中的数据
            parMap.put("tableName", tableName);
            parMap.put("timeDay", timeDay);
            // 删除并添加新的统计结果
            studyTimeService.calculationStudyTime(parMap);
        }
    }
}
