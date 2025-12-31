package com.cloud.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.model.user.SysDepartment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysDepartmentDao extends BaseMapper<SysDepartment> {


    @Select("select * from sys_department t where t.dname = #{dname}")
    SysDepartment findByName(String dName);

    int count(Map<String, Object> params);

    List<SysDepartment> findData(Map<String, Object> params);

    @Select("select parentid from sys_department")
    List<Long> parentIdList();

    @Select("with RECURSIVE detp as (\n" +
            " select detp1.* from sys_department detp1 where detp1.id=#{id}\n" +
            " union all\n" +
            " select detp2.* from sys_department detp2,detp where detp2.parentid=detp.id\n" +
            " )\n" +
            " select detp.id from detp")
    List<Long> findChildIdList(Long id);
}
