package com.cloud.user.schedule;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.user.dao.TaskDao;
import com.cloud.user.model.LcTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;


@Component
public class UpdateTaskState {


    @Autowired
    TaskDao taskDao;

    // 每天23点执行
    @Scheduled(cron = "0 0 23 * * ?")
    // @Scheduled(cron = "*/5 * * * * ?")
    public void updateTaskState() throws Exception {
        System.out.println("-----定时任务执行");
        LocalDateTime now = LocalDateTime.now();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentTime = sdf.format(Date.from(now.atZone(ZoneId.systemDefault()).toInstant())) + " 23:59:59";
        QueryWrapper<LcTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.le("plan_end_time", currentTime);
        queryWrapper.ne("state", 3);
        List<LcTask> lcTasks = taskDao.selectList(queryWrapper);
        if (lcTasks != null && lcTasks.size() > 0) {
            for (LcTask bean : lcTasks) {
                bean.setFlg(0);
                taskDao.updateById(bean);
            }
        }
    }
}
