package com.cloud.backend.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.model.common.IntDirect;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface IntDirectDao extends BaseMapper<IntDirect> {
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into int_direct(name, country, describe) values(#{name}, #{country}, #{describe})")
    int save(IntDirect intDirect);

    @Update("update int_direct  set name = #{name}, describe = #{describe},country=#{country} where id = #{id}")
    int update(IntDirect intDirect);

    @Delete("delete from int_direct where id = #{id}")
    int delete(Long id);

    @Select("select * from int_direct t where t.id = #{id}")
    IntDirect findById(Long id);

    @Select("select * from int_direct t where t.name = #{name}")
    IntDirect findByName(String name);

    int count(Map<String, Object> params);

    List<IntDirect> findData(Map<String, Object> params);
}
