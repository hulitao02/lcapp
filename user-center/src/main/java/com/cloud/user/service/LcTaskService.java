package com.cloud.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.core.ApiResult;
import com.cloud.user.model.LcTask;

public interface LcTaskService extends IService<LcTask> {
	void addTask(LcTask lcTask) throws Exception;

	ApiResult updateTask(LcTask lcTask) throws Exception;

	void delTask(LcTask lcTask) throws Exception;
}
