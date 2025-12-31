package com.cloud.user.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.model.user.AppUser;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.user.dao.*;
import com.cloud.user.model.LcTask;
import com.cloud.user.model.LcTaskState;
import com.cloud.user.model.LcTaskUser;
import com.cloud.user.service.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;


@Slf4j
@Service
public class LcTaskServiceImpl extends ServiceImpl<TaskDao, LcTask> implements LcTaskService {

    @Resource
    TaskDao taskDao;
    @Resource
    TaskUserDao taskUserDao;
    @Resource
    LcTaskStateDao lcTaskStateDao;
    @Resource
    AppUserDao appUserDao;



    @Override
    @Transactional
    public void addTask(LcTask lcTask) throws Exception {
        AppUser appUser = AppUserUtil.getLoginAppUser();
        lcTask.setCreateTime(new Timestamp(System.currentTimeMillis()));
        lcTask.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        // 1：未开始 2：进行中 3：已完成
        if (lcTask.getState()==2){
            lcTask.setRelStartTime(new Timestamp(System.currentTimeMillis()).toString());
        }else if (lcTask.getState()==3){
            lcTask.setRelEndTime(new Timestamp(System.currentTimeMillis()).toString());
        }
        boolean flg=false;
        taskDao.insert(lcTask);
        if (lcTask.getUserIds() != null && !lcTask.getUserIds().isEmpty()) {
            String[] userIds = lcTask.getUserIds().split(",");
            for (String userId : userIds) {
                if (Integer.parseInt(userId)==appUser.getId().intValue()){
                    flg=true;
                }
                LcTaskUser taskUser = new LcTaskUser();
                taskUser.setTaskId(lcTask.getId());
                taskUser.setUserId(Integer.parseInt(userId));
                AppUser user = appUserDao.selectById(Integer.parseInt(userId));
                if (user==null){
                    continue;
                }
                taskUser.setDeptId(user.getDepartmentId().intValue());
                taskUser.setFlg(0);
                taskUserDao.insert(taskUser);
            }
        }
        if (flg==false){
            LcTaskUser taskUser = new LcTaskUser();
            taskUser.setTaskId(lcTask.getId());
            taskUser.setUserId(appUser.getId().intValue());
            taskUser.setDeptId(appUser.getDepartmentId().intValue());
            taskUser.setFlg(1);
            taskUserDao.insert(taskUser);
        }
    }


    @Override
    @Transactional
    public ApiResult updateTask(LcTask lcTask) throws Exception {
        AppUser appUser = AppUserUtil.getLoginAppUser();
        lcTask.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        LcTask task = taskDao.selectById(lcTask.getId());
        if (task == null) {
            return ApiResultHandler.buildApiResult(100, "任务不存在", null);
        }
        int state = lcTask.getState();
        if (state != task.getState()) {
            if (lcTask.getState()==2){
                lcTask.setRelStartTime(new Timestamp(System.currentTimeMillis()).toString());
            }else if (lcTask.getState()==3){
                lcTask.setRelEndTime(new Timestamp(System.currentTimeMillis()).toString());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = sdf.parse(task.getPlanEndTime()+" 23:59:59");
                long planEndTime = date.getTime();
                long relEndTime = sdf.parse(lcTask.getRelEndTime()).getTime();
                // 实际完成时间晚于计划时间
                if (relEndTime>planEndTime){
                   lcTask.setFlg(0);
                }else {
                    lcTask.setFlg(1);
                }
            }
            LcTaskState taskState = new LcTaskState();
            taskState.setTaskId(lcTask.getId());
            taskState.setCreateUser(appUser.getId().intValue());
            taskState.setState(state);
            taskState.setCreateTime(new Timestamp(System.currentTimeMillis()));
            lcTaskStateDao.insert(taskState);
        }
        taskDao.updateById(lcTask);
        boolean flg=false;
        if (lcTask.getUserIds() != null && !lcTask.getUserIds().isEmpty()) {
            taskUserDao.delete(new LambdaQueryWrapper<LcTaskUser>().eq(LcTaskUser::getTaskId, lcTask.getId()));
            String[] userIds = lcTask.getUserIds().split(",");
            for (String userId : userIds) {
                if (Integer.parseInt(userId)==task.getCreateUser()){
                    flg=true;
                }
                LcTaskUser taskUser = new LcTaskUser();
                taskUser.setTaskId(lcTask.getId());
                taskUser.setUserId(Integer.parseInt(userId));
                AppUser user = appUserDao.selectById(Integer.parseInt(userId));
                if (user==null){
                    continue;
                }
                taskUser.setDeptId(user.getDepartmentId().intValue());
                taskUser.setFlg(0);
                taskUserDao.insert(taskUser);
            }
        } else {
            taskUserDao.delete(new LambdaQueryWrapper<LcTaskUser>().eq(LcTaskUser::getTaskId, lcTask.getId()));
        }
        LcTaskUser taskUser = new LcTaskUser();
        taskUser.setTaskId(lcTask.getId());
        if (flg==false){
            appUser = appUserDao.selectById(task.getCreateUser());
            if (appUser!=null){
                taskUser.setUserId(task.getCreateUser());
                taskUser.setDeptId(appUser.getDepartmentId().intValue());
                taskUser.setFlg(1);
                taskUserDao.insert(taskUser);
            }
        }
        return ApiResultHandler.buildApiResult(200, "操作成功", lcTask);
    }


    @Override
    @Transactional
    public void delTask(LcTask lcTask) throws Exception {
        taskDao.deleteById(lcTask.getId());
        taskUserDao.delete(new LambdaQueryWrapper<LcTaskUser>().eq(LcTaskUser::getTaskId, lcTask.getId()));
        lcTaskStateDao.delete(new LambdaQueryWrapper<LcTaskState>().eq(LcTaskState::getTaskId, lcTask.getId()));
    }

}
