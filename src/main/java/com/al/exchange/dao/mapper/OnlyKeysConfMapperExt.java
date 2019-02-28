package com.al.exchange.dao.mapper;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OnlyKeysConfMapperExt {


    @Select("select only_key from only_keys_conf")
    List<String> selectOnlyKeys();

}