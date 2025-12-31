package com.cloud.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.model.common.Page;
import com.cloud.model.user.AppUser;
import com.cloud.model.user.SysDepartment;
import com.cloud.user.dao.AppUserDao;
import com.cloud.user.service.SysDepartmentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SysDepartmentController {
    @Resource
    private SysDepartmentService sysDepartmentService;


    /**
     * 管理后台添加部门
     *
     * @param sysDepartment
     * @return
     */
    //@PreAuthorize("hasAuthority('back:sysdepartment:save')")
    @PostMapping("/sysDepartment")
    public SysDepartment save(@RequestBody SysDepartment sysDepartment) {
        if (StringUtils.isBlank(sysDepartment.getDname())) {
            throw new IllegalArgumentException("部门名称不能为空");
        }
        sysDepartmentService.add(sysDepartment);
        return sysDepartment;
    }


    @GetMapping("/sysDepartment/{id}")
    public SysDepartment getSysDepartmentById(@PathVariable Long id) {
        return sysDepartmentService.findById(id);
    }

    /**
     * 管理后台修改部门
     *
     * @param sysDepartment
     */
//    @LogAnnotation(module = LogModule.UPDATE_PERMISSION)
    //@PreAuthorize("hasAuthority('back:sysdepartment:update')")
    @PutMapping("/sysDepartment")
    public SysDepartment update(@RequestBody SysDepartment sysDepartment) {
        if (StringUtils.isBlank(sysDepartment.getDname())) {
            throw new IllegalArgumentException("部门名称不能为空");
        }
        sysDepartmentService.update(sysDepartment);
        return sysDepartment;
    }

    /**
     * 删除部门
     *
     * @param id
     */
    //todo 没有子部门可直接删除
    //@PreAuthorize("hasAuthority('back:sysdepartment:delete')")
    @DeleteMapping("/sysDepartment/{id}")
    public void delete(@PathVariable Long id) {
        sysDepartmentService.delete(id);
    }


    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    //@PreAuthorize("hasAuthority('back:sysdepartment:query')")
    @GetMapping("/sysDepartment")
    public Page<SysDepartment> findPermissions(@RequestParam Map<String, Object> params) {
        return sysDepartmentService.findByPage(params);
    }


    /**
     * 菜单table
     *
     * @param parentId
     * @param all
     * @param list
     */
    private void setSortTable(Long parentId, List<SysDepartment> all, List<SysDepartment> list) {
        all.forEach(a -> {
            if (a.getParentId().longValue() == parentId.longValue()) {
                list.add(a);
                setSortTable(a.getId(), all, list);
            }
        });
    }


    @Autowired
    AppUserDao appUserDao;

    /**
     * 查询所有菜单
     */
    @GetMapping("/sysDepartment/all")
    public List<SysDepartment> findAll() {
        List<SysDepartment> all = sysDepartmentService.findAll();
        List<SysDepartment> newList = new ArrayList<>();
        if (all != null && all.size() > 0) {
            for (SysDepartment sysDepartment : all) {
                Long deptId = sysDepartment.getId();
                QueryWrapper<AppUser> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("department_id", deptId);
                queryWrapper.eq("status", 1);
                Integer integer = appUserDao.selectCount(queryWrapper);
                sysDepartment.setPersonCount(integer);
                newList.add(sysDepartment);
            }
        }
        List<SysDepartment> list = new ArrayList<>();
        setSortTable(0L, newList, list);
        return newList;
    }


    /**
     * @author:胡立涛
     * @description: TODO 查询所有部门及部门下的人员信息
     * @date: 2025/12/23
     * @param:
     * @return: com.cloud.core.ApiResult
     */
    @GetMapping("/sysDepartment/getAll")
    public ApiResult getAll() {
        try {
            List<SysDepartment> all = sysDepartmentService.findAll();
            List<SysDepartment> newList = new ArrayList<>();
            if (all != null && all.size() > 0) {
                for (SysDepartment sysDepartment : all) {
                    Long deptId = sysDepartment.getId();
                    QueryWrapper<AppUser> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("department_id", deptId);
                    queryWrapper.eq("status", 1);
                    Integer integer = appUserDao.selectCount(queryWrapper);
                    sysDepartment.setPersonCount(integer);
                    newList.add(sysDepartment);
                }
            }
            List<SysDepartment> list = new ArrayList<>();
            setSortTable(0L, newList, list);
            for (SysDepartment bean:newList){
                // 查询部门下的用户信息
                QueryWrapper<AppUser> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("department_id", bean.getId());
                queryWrapper.eq("status", 1);
                queryWrapper.orderByDesc("id");
                List<AppUser> appUsers = appUserDao.selectList(queryWrapper);
                if (appUsers != null && appUsers.size() > 0) {
                    for (AppUser appUser : appUsers) {
                        appUser.setPassword(null);
                        appUser.setDname(appUser.getNickname());
                    }
                }
                bean.setAppUsers(appUsers);
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", newList);
        }catch (Exception e){
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * 知识点树
     *
     * @param parentId
     * @param all
     * @param list
     */
    private void setMenuTree(Long parentId, List<SysDepartment> all, List<SysDepartment> list) {
        all.forEach(sd -> {
            if (parentId.longValue() == sd.getParentId().longValue()) {
                list.add(sd);
                List<SysDepartment> child = new ArrayList<>();
                sd.setChild(child);
                setMenuTree(sd.getId(), all, child);
            }
        });
    }

    @GetMapping("/sysDepartment/tree")
    public List<SysDepartment> findMenuTree() {
        List<SysDepartment> all = sysDepartmentService.findAll();
        List<SysDepartment> list = new ArrayList<>();
        setMenuTree(0L, all, list);
        return list;
    }

    @GetMapping("/findSysDepartmentList")
    public List<SysDepartment> findSysDepartmentList() {
        return sysDepartmentService.findAll();
    }


}
