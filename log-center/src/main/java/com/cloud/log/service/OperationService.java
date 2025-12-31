package com.cloud.log.service;

import com.cloud.model.common.Page;
import com.cloud.model.log.OperationLog;

import java.util.List;
import java.util.Map;

public interface OperationService {
    /**
     * 保存日志
     *
     * @param log
     */
    void save(OperationLog log);

    Page<OperationLog> findLogs(Map<String, Object> params);

    public List<OperationLog> findLogsByName(Map<String, Object> params);
}
