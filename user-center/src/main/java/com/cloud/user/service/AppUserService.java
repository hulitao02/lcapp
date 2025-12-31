package com.cloud.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.common.Page;
import com.cloud.model.user.AppUser;
import com.cloud.model.user.LoginAppUser;
import com.cloud.model.user.SysPermission;
import com.cloud.model.user.SysRole;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AppUserService extends IService<AppUser> {

	void addAppUser(AppUser appUser);

	void updateAppUser(AppUser appUser);

	LoginAppUser findByUsername(String username);

	AppUser findById(Long id);

	void setRoleToUser(Long id, Set<Long> roleIds);

	void updatePassword(Long id, String oldPassword, String newPassword);

	Page<AppUser> findUsers(Map<String, Object> params);

	Set<SysRole> findRolesByUserId(Long userId);

	void bindingPhone(Long userId, String phone);

	Boolean  RunManagerInterface(Map<String,String> param);

	Set<SysPermission> findPermissionsByRoleId(Long roleId);

	//zhangsheng add 2021-7-6
	public List<AppUser> getDeptUserIds(Long deptId);


	List<AppUser> getAppUserByRole(String roleCode);

    List<AppUser> getAppUserListByUserIdList(List<Long> userIdList);

	boolean removeById(Serializable id);

	/**
	 * @author: 胡立涛
	 * @description: TODO 学员批量导入
	 * @date: 2022/6/2
	 * @param: [file, deptId]
	 * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
	 */
	List<Map<String, Object>> uploadInfo(Sheet sheet, Long deptId);

	boolean existUserInDept(Long deptId);
}
