package com.cloud.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ExamConstants;
import com.cloud.model.common.Page;
import com.cloud.model.user.SysPermission;
import com.cloud.model.user.SysRole;
import com.cloud.model.user.SysRoleUser;
import com.cloud.user.dao.RolePermissionDao;
import com.cloud.user.dao.SysRoleDao;
import com.cloud.user.dao.UserRoleDao;
import com.cloud.user.service.SysRoleService;
import com.cloud.utils.PageUtil;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysRoleServiceImpl implements SysRoleService {

	@Autowired
	private SysRoleDao sysRoleDao;
	@Autowired
	private UserRoleDao userRoleDao;
	@Autowired
	private RolePermissionDao rolePermissionDao;
	@Autowired
	private AmqpTemplate amqpTemplate;

	@Transactional
	@Override
	public Integer save(SysRole sysRole) {
		SysRole role = sysRoleDao.findByCode(sysRole.getCode());
		if (role != null) {
			throw new IllegalArgumentException("角色code已存在");
		}

		sysRole.setCreateTime(new Date());
		sysRole.setUpdateTime(sysRole.getCreateTime());

		log.info("保存角色：{}", sysRole);
		return sysRoleDao.save(sysRole);
	}

	@Transactional
	@Override
	public void update(SysRole sysRole) {
		sysRole.setUpdateTime(new Date());

		sysRoleDao.update(sysRole);
		log.info("修改角色：{}", sysRole);
	}


	@Transactional
	@Override
	public void deleteRole(Long id) {
		SysRole sysRole = sysRoleDao.findById(id);
		if (sysRole == null) {
			throw new IllegalArgumentException("角色不存在");
		}
		if (ExamConstants.SYSTEM_SUPER_ADMIN.equals(sysRole.getCode())) {
			throw new IllegalArgumentException("管理员不可删除");
		}
		QueryWrapper<SysRoleUser> qw = new QueryWrapper<>();
		qw.eq("roleid", id);
		long count = userRoleDao.selectCount(qw);
		if (count>0){
			throw new IllegalArgumentException("角色已被用户引用，不可删除");
		}
		sysRoleDao.delete(id);
		rolePermissionDao.deleteRolePermission(id, null);
		log.info("删除角色：{}", sysRole);

		// 发布role删除的消息
		//amqpTemplate.convertAndSend(UserCenterMq.MQ_EXCHANGE_USER, UserCenterMq.ROUTING_KEY_ROLE_DELETE, id);
	}

	/**
	 * 给角色设置权限
	 *
	 * @param roleId
	 * @param permissionIds
	 */
	@Transactional
	@Override
	public void setPermissionToRole(Long roleId, Set<Long> permissionIds) {
		SysRole sysRole = sysRoleDao.findById(roleId);
		if (sysRole == null) {
			throw new IllegalArgumentException("角色不存在");
		}

		// 查出角色对应的old权限
		Set<Long> oldPermissionIds = rolePermissionDao.findPermissionsByRoleIds(Sets.newHashSet(roleId)).stream()
				.map(p -> p.getId()).collect(Collectors.toSet());

		// 需要添加的权限
		Collection<Long> addPermissionIds = org.apache.commons.collections4.CollectionUtils.subtract(permissionIds,
				oldPermissionIds);
		if (!CollectionUtils.isEmpty(addPermissionIds)) {
			addPermissionIds.forEach(permissionId -> {
				rolePermissionDao.saveRolePermission(roleId, permissionId);
			});
		}
		// 需要移除的权限
		Collection<Long> deletePermissionIds = org.apache.commons.collections4.CollectionUtils
				.subtract(oldPermissionIds, permissionIds);
		if (!CollectionUtils.isEmpty(deletePermissionIds)) {
			deletePermissionIds.forEach(permissionId -> {
				rolePermissionDao.deleteRolePermission(roleId, permissionId);
			});
		}

		log.info("给角色id：{}，分配权限：{}", roleId, permissionIds);
	}

	@Override
	public SysRole findById(Long id) {
		return sysRoleDao.findById(id);
	}

	@Override
	public Page<SysRole> findRoles(Map<String, Object> params) {
		int total = sysRoleDao.count(params);
		List<SysRole> list = Collections.emptyList();
		if (total > 0) {
			PageUtil.pageParamConver(params, false);

			list = sysRoleDao.findData(params);
		}
		return new Page<>(total, list);
	}

	@Override
	public Set<SysPermission> findPermissionsByRoleId(Long roleId) {
		return rolePermissionDao.findPermissionsByRoleIds(Sets.newHashSet(roleId));
	}

	@Override
	public void setKpIdsToRole(Long roleId, Set<String> kpIds) {
		sysRoleDao.deleteRoleKpId(roleId);
		kpIds.stream().forEach(e->{
			sysRoleDao.addKpIdRole(roleId,e);
		});
	}

	@Override
	public void addTest(Long roleId, Long kpId) {
		sysRoleDao.addTest(roleId,kpId);
	}

	@Override
	public List<String> getKpIdsByRoleId(Long roleId) {
		return sysRoleDao.getKpIdsByRoleId(roleId);
	}
}
