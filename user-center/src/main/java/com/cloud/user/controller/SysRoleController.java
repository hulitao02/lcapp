package com.cloud.user.controller;

import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.exception.ResultMesCode;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.model.common.KnowledgePoints;
import com.cloud.model.common.Page;
import com.cloud.model.log.LogAnnotation;
import com.cloud.model.log.constants.LogModule;
import com.cloud.model.user.SysPermission;
import com.cloud.model.user.SysRole;
import com.cloud.user.service.AppUserService;
import com.cloud.user.service.SysRoleService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;
    private Logger logger = LoggerFactory.getLogger(SysRoleController.class);

    /**
     * 管理后台添加角色
     *
     * @param sysRole
     */
    @LogAnnotation(module = LogModule.ADD_ROLE)
    @PreAuthorize("hasAuthority('back:role:save')")
    @PostMapping("/roles")
    public SysRole save(@RequestBody SysRole sysRole) {
        if (StringUtils.isBlank(sysRole.getCode())) {
            throw new IllegalArgumentException("角色code不能为空");
        }
        if (StringUtils.isBlank(sysRole.getName())) {
            sysRole.setName(sysRole.getCode());
        }

        HashSet<String> set = new HashSet<>();
        int id = 0;
        try {
            id = sysRoleService.save(sysRole);
        } catch (Exception e) {
            logger.error("创建角色失败：{}", e);
        }
        try {
            if (id > 0) {
                sysRoleService.setKpIdsToRole(sysRole.getId(), set);
            }
        } catch (Exception e) {
            logger.error("创建角色时给角色添加知识点失败：{}", e);
        }

        return sysRole;
    }

    /**
     * 管理后台删除角色
     *
     * @param id
     */
    @LogAnnotation(module = LogModule.DELETE_ROLE)
    @PreAuthorize("hasAuthority('back:role:delete')")
    @DeleteMapping("/roles/{id}")
    public void deleteRole(@PathVariable Long id) {
        sysRoleService.deleteRole(id);
    }

    /**
     * 管理后台修改角色
     *
     * @param sysRole
     */
    @LogAnnotation(module = LogModule.UPDATE_ROLE)
    @PreAuthorize("hasAuthority('back:role:update')")
    @PutMapping("/roles")
    public SysRole update(@RequestBody SysRole sysRole) {
        if (StringUtils.isBlank(sysRole.getName())) {
            throw new IllegalArgumentException("角色名不能为空");
        }

        sysRoleService.update(sysRole);

        return sysRole;
    }

    /**
     * 管理后台给角色分配权限
     *
     * @param id            角色id
     * @param permissionIds 权限ids
     */
    @LogAnnotation(module = LogModule.SET_PERMISSION)
    @PreAuthorize("hasAuthority('back:role:permission:set')")
    @PostMapping("/roles/{id}/permissions")
    public void setPermissionToRole(@PathVariable Long id, @RequestBody Set<Long> permissionIds) {
        sysRoleService.setPermissionToRole(id, permissionIds);
    }

    /**
     * 获取角色的权限
     *
     * @param id
     */
    @PreAuthorize("hasAnyAuthority('back:role:permission:set','role:permission:byroleid')")
    @GetMapping("/roles/{id}/permissions")
    public Set<SysPermission> findPermissionsByRoleId(@PathVariable Long id) {
        return sysRoleService.findPermissionsByRoleId(id);
    }

    @PreAuthorize("hasAuthority('back:role:query')")
    @GetMapping("/roles/{id}")
    public SysRole findById(@PathVariable Long id) {
        return sysRoleService.findById(id);
    }

    /**
     * 搜索角色
     *
     * @param params
     */
    @PreAuthorize("hasAuthority('back:role:query')")
    @GetMapping("/roles")
    public Page<SysRole> findRoles(@RequestParam Map<String, Object> params) {
        return sysRoleService.findRoles(params);
    }

    /**
     * 给角色分配知识点
     */
    @PostMapping(value = "addKpIdsToRole")
    public ApiResult addKpIdsToRole(@RequestBody Map<String, Object> map) {
        try {
            Object roleId = map.get("roleId");
            List<Object> kpIds = (List<Object>) map.get("kpIds");
            Set<String> kk = new HashSet<>();
            kpIds.stream().forEach(e -> kk.add(String.valueOf(e)));
            sysRoleService.setKpIdsToRole(Long.valueOf(String.valueOf(roleId)), kk);
            return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(), "角色分配知识点成功", null);
        } catch (Exception e) {
            logger.error("角色分配知识点失败：", e);
            return ApiResultHandler.buildApiResult(500, "角色分配知识点失败", e.toString());
        }
    }


    @Autowired
    private AppUserService appUserService;

    /**
     * 查询角色拥有的知识点
     */
    @GetMapping(value = "/getKpIdsByRoleId/{roleId}")
    public ApiResult getKpIdsByRoleId(@PathVariable("roleId") Long roleId) {
        try {
//			this.appUserService.list();
            List<String> list = sysRoleService.getKpIdsByRoleId(roleId);
            return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(), "查询角色拥有的知识点成功", list);
        } catch (Exception e) {
            logger.error("角色分配知识点失败：", e);
            return ApiResultHandler.buildApiResult(500, "查询角色拥有的知识点失败", null);
        }
    }

    /**
     * 给角色分配知识点
     */
    @GetMapping(value = "tetsaddKpIdsToRole")
    public ApiResult tetsaddKpIdsToRole() {
        try {
            String roles = "1,11,12,25,26,10,27";
            String[] split = roles.split(",");
            List<KnowledgePoints> all = manageBackendFeign.findAll();
            Set<Long> set = new HashSet<>();
            all.stream().forEach(e -> set.add(e.getId()));
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                Long roleId = Long.valueOf(s);
                for (Long kpId : set) {
                    sysRoleService.addTest(roleId, kpId);
                }
            }

            return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(), "角色分配知识点成功", null);
        } catch (Exception e) {
            logger.error("角色分配知识点失败：", e);
            return ApiResultHandler.buildApiResult(500, "角色分配知识点失败", null);
        }
    }

    @Autowired
    private ManageBackendFeign manageBackendFeign;
}
