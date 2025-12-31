package com.cloud.user.dao;

import com.cloud.model.user.UserPosition;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * @author Red
 */
@Mapper
public interface UserPositionDao {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into user_position(name, createtime, updatetime,creator,positiondesc) "
            + "values(#{name}, #{createTime}, #{updateTime}, #{creator},#{positionDesc})")
    int save(UserPosition userPosition);

    int update(UserPosition userPosition);

    @Select("select * from user_position t where t.id = #{id}")
    UserPosition findById(Long id);

    int count(Map<String, Object> params);

    List<UserPosition> findData(Map<String, Object> params);

    @Delete("delete from user_position t where t.id = #{id}")
    int deleteById(Long id);

    @Select("select * from user_position")
    List<UserPosition> findUserPosition();

}
