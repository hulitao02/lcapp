package com.cloud.backend.dao;

import com.cloud.backend.model.Menu;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 角色菜单关系<br>
 * 角色和菜单是多对多关系，role_menu是中间表
 *
 * @author 数据管理
 */
@Mapper
public interface RoleMenuDao {

	@Insert("insert into role_menu(roleId, menuId) values(#{roleId}, #{menuId})")
	int save(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

	int delete(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

	@Select("select t.menuId from role_menu t where t.roleId = #{roleId}")
	Set<Long> findMenuIdsByRoleId(Long roleId);

	List<Menu> findMenusByRoleIds(@Param("roleIds") Set<Long> roleIds);


	/**
	 *
	 * @author:胡立涛
	 * @description: TODO 根据roleIdList查询菜单列表
	 * @date: 2022/3/15
	 * @param: [roleIdList]
	 * @return: java.util.List<java.util.Map>
	 */
	List<Map> getMenuByRoleIds(@Param("roleIdList") List roleIdList);
}
