package com.cloud.backend.controller;

import com.cloud.backend.model.CountryDict;
import com.cloud.backend.service.IntDirectService;
import com.cloud.feign.exam.ExamClient;
import com.cloud.model.common.IntDirect;
import com.cloud.model.common.Page;
import com.cloud.model.log.LogAnnotation;
import com.cloud.model.log.constants.LogModule;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class IntDirectController {
    @Resource
    private IntDirectService intDirectService;
    @Resource
    private ExamClient examClient;

    /**
     * 管理后台添加情报方向
     *
     * @param intDirect
     * @return
     */
    //@LogAnnotation(module = LogModule.ADD_PERMISSION)
    @PreAuthorize("hasAuthority('back:intdirect:save')")
    @PostMapping("/intDirect")
    public IntDirect save(@RequestBody IntDirect intDirect) {
        if (StringUtils.isBlank(intDirect.getName())) {
            throw new IllegalArgumentException("情报方向名称不能为空");
        }
        if (StringUtils.isBlank(intDirect.getCountry())) {
            throw new IllegalArgumentException("国家不能为空");
        }

        intDirectService.save(intDirect);
        return intDirect;
    }

    /**
     * 管理后台修改情报方向
     *
     * @param intDirect
     */
    @LogAnnotation(module = LogModule.UPDATE_PERMISSION)
    @PreAuthorize("hasAuthority('back:intdirect:update')")
    @PutMapping("/intDirect")
    public IntDirect update(@RequestBody IntDirect intDirect) {
        if (StringUtils.isBlank(intDirect.getName())) {
            throw new IllegalArgumentException("情报方向名称不能为空");
        }
        if (StringUtils.isBlank(intDirect.getCountry())) {
            throw new IllegalArgumentException("国家不能为空");
        }
        intDirectService.update(intDirect);

        return intDirect;
    }

    /**
     * 删除权限标识
     *
     * @param id
     */
    //@LogAnnotation(module = LogModule.DELETE_PERMISSION)
    @PreAuthorize("hasAuthority('back:intdirect:delete')")
    @DeleteMapping("/intDirect/{id}")
    public void delete(@PathVariable Long id)
    {
        //删除前判断是否被绑定
        if (examClient.selectByDirectId(id)){
           throw new IllegalArgumentException("情报方向已经被试题绑定，无法删除！");
        }
        intDirectService.delete(id);
    }

    /**
     * 查询情报方向信息
     */
    @PreAuthorize("hasAuthority('back:intdirect:query')")
    @GetMapping("/intDirect")
    public Page<IntDirect> findPermissions(@RequestParam Map<String, Object> params) {
        return intDirectService.findByPage(params);
    }


    @GetMapping("/CountryDictAll")
    public List<CountryDict> CountryDictAll(){
        return intDirectService.findAll();
    }

    @GetMapping("/findDirectCountryAll")
    public List<IntDirect> findDirectCountryAll() {
        return intDirectService.findDirectCountryAll();
    }

    @GetMapping("/findIntDirectById/{id}")
    public IntDirect findIntDirectById(@PathVariable Long id) {
        return intDirectService.findById(id);
    }

    @PostMapping("/findIntDirectMap")
    public Map<Long, String> findIntDirectMap(@RequestBody List<Long> idList) {
        if (CollectionUtils.isNotEmpty(idList)) {
            List<IntDirect> list = intDirectService.lambdaQuery().select(IntDirect::getId, IntDirect::getName)
                    .in(IntDirect::getId, idList).list();
            if (CollectionUtils.isNotEmpty(list)) {
                Map<Long, String> map = list.stream().collect(Collectors.toMap(IntDirect::getId, IntDirect::getName));
                return map;
            }
        }
        return Collections.emptyMap();
    }
}
