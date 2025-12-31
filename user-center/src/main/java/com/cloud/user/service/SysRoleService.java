package com.cloud.user.service;

import com.cloud.model.common.Page;
import com.cloud.model.user.SysPermission;
import com.cloud.model.user.SysRole;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SysRoleService {

	Integer save(SysRole sysRole);

	void update(SysRole sysRole);

	void deleteRole(Long id);

	void setPermissionToRole(Long id, Set<Long> permissionIds);

	SysRole findById(Long id);

	Page<SysRole> findRoles(Map<String, Object> params);

	Set<SysPermission> findPermissionsByRoleId(Long roleId);

    void setKpIdsToRole(Long roleId, Set<String> kpIds);

	void addTest(Long roleId, Long kpId);

	List<String> getKpIdsByRoleId(Long roleId);
}
