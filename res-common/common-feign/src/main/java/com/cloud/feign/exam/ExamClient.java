package com.cloud.feign.exam;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @author md
 * @date 2021/4/20 11:32
 */
@Component
@FeignClient(name = "exam-center")
public interface ExamClient {

    @GetMapping("selectByDirectId/{id}")
    Boolean selectByDirectId(@PathVariable("id") Long id);

    @PostMapping("/isUserRelated")
    Map<Long, String> isUserRelated(@RequestBody List<Long> userIdList);
}
