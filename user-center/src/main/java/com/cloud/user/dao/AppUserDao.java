package com.cloud.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.model.user.AppUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AppUserDao extends BaseMapper<AppUser> {

	@Options(useGeneratedKeys = true, keyProperty = "id")
	@Insert("insert into app_user(username, password, nickname, department_id,position_id,rank_num,head_img_url, phone, sex, enabled, type,level, create_time, update_time) "
			+ "values(#{username}, #{password}, #{nickname}, #{departmentId},#{positionId},#{rankNum},#{headImgUrl}, #{phone}, #{sex}, #{enabled}, #{type},#{level}, #{createTime}, #{updateTime})")
	int save(AppUser appUser);

	int update(AppUser appUser);

	@Select("select * from app_user t where t.id = #{id}")
	AppUser findById(Long id);

	int count(Map<String, Object> params);

	List<AppUser> findData(Map<String, Object> params);





	@Select("select department_id from app_user")
	List<Long> departmentList();

	@Select("select position_id from app_user")
	List<Long> positionList();


	List<AppUser> getAppUserByRole(String roleCode);


	/**
	 *
	 * @author: 胡立涛
	 * @description: TODO 根据登录名查询用户信息
	 * @date: 2022/6/1
	 * @param: [userName]
	 * @return: java.util.List<java.util.Map>
	 */
	List<Map> getUsers(Map<String,Object> map);


	/**
	 *
	 * @author: 胡立涛
	 * @description: TODO 保存用户信息
	 * @date: 2022/6/2
	 * @param: [map]
	 * @return: void
	 */
	void saveInfo(Map map);




}
