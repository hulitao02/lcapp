package com.cloud.backend.dao;

import com.cloud.backend.model.CountryDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CountryDictDao {

    @Select("select * from country_dict")
    List<CountryDict> findAll();


}
