package com.al.exchange.dao.mapper;

import com.al.exchange.dao.domain.OnlyKeysConf;
import com.al.exchange.dao.domain.OnlyKeysConfExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OnlyKeysConfMapper {
    long countByExample(OnlyKeysConfExample example);

    int deleteByExample(OnlyKeysConfExample example);

    int deleteByPrimaryKey(String onlyKey);

    int insert(OnlyKeysConf record);

    int insertSelective(OnlyKeysConf record);

    List<OnlyKeysConf> selectByExample(OnlyKeysConfExample example);

    OnlyKeysConf selectByPrimaryKey(String onlyKey);

    int updateByExampleSelective(@Param("record") OnlyKeysConf record, @Param("example") OnlyKeysConfExample example);

    int updateByExample(@Param("record") OnlyKeysConf record, @Param("example") OnlyKeysConfExample example);

    int updateByPrimaryKeySelective(OnlyKeysConf record);

    int updateByPrimaryKey(OnlyKeysConf record);
}