package com.cloud.user.dao;

import com.cloud.model.user.SysRole;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysRoleDao {

	@Options(useGeneratedKeys = true, keyProperty = "id")
	@Insert("insert into sys_role(code, name, createTime, updateTime) values(#{code}, #{name}, #{createTime}, #{createTime})")
	int save(SysRole sysRole);

	@Update("update sys_role  set name = #{name} ,updateTime = #{updateTime} where id = #{id}")
	int update(SysRole sysRole);

	@Select("select * from sys_role t where t.id = #{id}")
	SysRole findById(Long id);

	@Select("select * from sys_role t where t.code = #{code}")
	SysRole findByCode(String code);

	@Delete("delete from sys_role where id = #{id}")
	int delete(Long id);

	int count(Map<String, Object> params);

	List<SysRole> findData(Map<String, Object> params);

	@Delete("delete from role_kp where role_id = #{roleId}")
    void deleteRoleKpId(Long roleId);

	@Insert("insert into role_kp(role_id,kp_id) values(#{roleId}, #{kpId})")
	void addKpIdRole(@Param("roleId") Long roleId, @Param("kpId") String kpId);

	@Insert("insert into role_kp(role_id,kp_id) values(#{roleId}, #{kpId})")
	void addTest(@Param("roleId") Long roleId, @Param("kpId") Long kpId);

	@Select("select kp_id from role_kp t where t.role_id = #{roleId}")
	List<String> getKpIdsByRoleId(Long roleId);
}
