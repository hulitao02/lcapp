package com.cloud.feign.usercenter;

import com.cloud.config.FeignConfig;
import com.cloud.model.common.Page;
import com.cloud.model.user.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by dyl on 2021/03/23.
 * 聚合接口
 */
@Component
@FeignClient(name = "user-center",configuration = FeignConfig.class)
public interface SysDepartmentFeign {


    //获取所有单位
    @RequestMapping("/findSysDepartmentList")
    public List<SysDepartment> findSysDepartmentList();

    //树形结构
    @GetMapping("/sysDepartment/all")
    public List<SysDepartment> findAll();


    /**
     * 当前登录用户 LoginAppUser
     */
    @RequestMapping("/users/current")
    public LoginAppUser getLoginAppUser();

    @RequestMapping(value = "/users-anon/internal", params = "username")
    public LoginAppUser findByUsername(String username);

    @RequestMapping(value = "/users-anon/register",method = RequestMethod.POST)
    public AppUser register(AppUser appUser) ;
    /**
     * 用户查询
     *
     * @param params
     */
    @RequestMapping("/users")
    public Page<AppUser> findUsers(@RequestParam Map<String, Object> params);

    @PostMapping("/getUsers")
    public List<AppUser> getUsers(@RequestBody AppUser appUser);

    @GetMapping("/users/{id}")
    public AppUser findUserById(@PathVariable("id") Long id);

    @GetMapping("/users/{id}/roles")
    public Set<SysRole> findRolesByUserId(@PathVariable("id") Long id);

    @GetMapping("/getPermissonByUserId")
    public Set<SysPermission>  getPermissonByUserId(@RequestParam("userid") Long userid);

    @GetMapping("/findAppUserById")
    public AppUser findAppUserById(@RequestParam("userid") Long userid);

    @GetMapping("/getUserByIdCard")
    public AppUser  getUserByIdCard(@RequestParam("idCard") String idCard);

    @GetMapping("/getAllUsers")
    public List<AppUser> getAllUsers();

    @GetMapping("/sysDepartment/{id}")
    public SysDepartment findSysDepartmentById(@PathVariable("id") Long id);

	/**
     * 获取部门内所有用户ID
     * @param deptId
     * @return
     */
    @GetMapping("/getDeptUserIds/{deptId}")
    public List<AppUser> getDeptUserIds(@PathVariable("deptId") Long deptId);



    /**
     *
     * @author:胡立涛
     * @description: TODO 获取阅卷组、监考组人员
     * @date: 2021/12/7
     * @param: [roleCode]
     * @return: java.util.List<com.cloud.model.user.AppUser>
     */
    @GetMapping("getAppUserByRole")
    List<AppUser> getAppUserByRole(@RequestParam("roleCode") String roleCode);


    /**
     *  通过用户ID查询用户信息
     * @param userIdArray
     * @return
     */
    @RequestMapping(value = "/users/getAppUserListByIds",method = RequestMethod.POST)
    public List<AppUser> getAppUserListByUserIdList(@RequestParam("userIdArray") Long[] userIdArray);

    /**
     *  通过用户ID查询用户拥有的知识点
     * @param userId
     * @return Set<Long></>
     */
    @RequestMapping(value = "/getKpIdsbyUserId",method = RequestMethod.GET)
    public Set<String> getKpIdsbyUserId(@RequestParam("userId") Long userId);


    /**
     *
     * @author:胡立涛
     * @description: TODO 获取用户知识点权限
     * @date: 2022/9/13
     * @param: [userid]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @param userid
     */
    @GetMapping("/getUserKpIds")
    List<Map<String,Object>> getUserKpIds(@RequestParam("userIds") Long[] userIds);
}
