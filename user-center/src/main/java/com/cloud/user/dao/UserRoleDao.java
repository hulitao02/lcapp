package com.cloud.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.model.user.SysPermission;
import com.cloud.model.user.SysRole;
import com.cloud.model.user.SysRoleUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户角色关系<br>
 * 用户和角色是多对多关系，sys_role_user是中间表
 *
 * @author 数据管理
 */
@Mapper
public interface UserRoleDao extends BaseMapper<SysRoleUser> {

	int deleteUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

	@Insert("insert into sys_role_user(userId, roleId) values(#{userId}, #{roleId})")
	int saveUserRoles(@Param("userId") Long userId, @Param("roleId") Long roleId);

	/**
	 * 根据用户id获取角色
	 *
	 * @param userId
	 * @return
	 */
	@Select("select r.* from sys_role_user ru inner join sys_role r on r.id = ru.roleId where ru.userId = #{userId}")
	Set<SysRole> findRolesByUserId(Long userId);

	@Select({"select sp.* from sys_permission sp inner join sys_role_permission srp on srp.permissionid = sp.id inner join sys_role sr on sr.id =srp.roleid where  srp.roleid=#{roleId}"})
	Set<SysPermission> findPermissionsByRoleId(Long roleId);


	/**
	 *
	 * @author:胡立涛
	 * @description: TODO 查询用户所拥有的知识点权限
	 * @date: 2022/9/13
	 * @param: [map]
	 * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
	 */
	List<Map<String,Object>> getUserKpIds(@Param("userIds") Long[] userIds);
}
