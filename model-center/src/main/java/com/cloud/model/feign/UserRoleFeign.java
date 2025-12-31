package com.cloud.model.feign;


import com.cloud.model.user.SysPermission;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Set;

@Component
@FeignClient(name = "user-center")
public interface UserRoleFeign {

    //获取所有单位
    @GetMapping("/findPermissionsById/{id}")
    public Set<SysPermission> findPermissionsById(@PathVariable("id") Long id);

}
