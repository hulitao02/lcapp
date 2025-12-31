package com.cloud.feign.log;


import com.cloud.config.FeignConfig;
import com.cloud.model.log.OperationLog;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "log-center",configuration = {FeignConfig.class})
public interface LogFeign {


    @GetMapping("/operationLog/findLog")
    public List<OperationLog> findLog(@RequestParam Map<String, Object> params);
}
