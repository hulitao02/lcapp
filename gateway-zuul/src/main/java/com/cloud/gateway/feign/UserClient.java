package com.cloud.gateway.feign;

import com.cloud.model.user.AppUser;
import com.cloud.model.user.SysPermission;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

/**
 * @author md
 * @date 2021/4/14 18:39
 */
@FeignClient("user-center")
public interface UserClient {

    @GetMapping("/findPermissionsById/{id}")
    public Set<SysPermission> findPermissionsById(@PathVariable("id") Long id);
    /**
     * 根据登录用户名获取用户
     */
    @GetMapping("/getAppUsersByLoginName")
    public List<AppUser> getAppUsersByloginname(@RequestParam("loginName") String loginName);



}
