package com.cloud.backend.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.model.common.Dict;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface DictDao extends BaseMapper<Dict> {
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into backend_dict(dictName, dictValue, dictType, dictDescription) values(#{dictName}, #{dictValue}, #{dictType}, #{dictDescription})")
    int save(Dict dict);

    @Update("update backend_dict  set dictName = #{dictName}, dictValue = #{dictValue}, dictType = #{dictType}, dictDescription = #{dictDescription} where id = #{id}")
    int update(Dict dict);

    @Delete("delete from backend_dict where id = #{id}")
    int delete(Long id);

    @Select("select * from backend_dict t where t.id = #{id}")
    Dict findById(Long id);

    @Select("select * from backend_dict t where t.dictName = #{dictName}")
    Dict findBydictName(String dictName);

    int count(Map<String, Object> params);

    List<Dict> findData(Map<String, Object> params);

    String getThemeType();

    void setThemeType(Long id) ;

    List<Dict> getThemeTypeList(Map<String, Object> params);

    Dict getLogoDict(@Param("dictType") String dictType,@Param("dictName")  String dictName);
}
