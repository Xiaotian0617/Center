package com.al.exchange.dao.mapper;

import com.al.exchange.dao.domain.AbnormalChange;
import com.al.exchange.dao.domain.AbnormalChangeExample;
import com.al.exchange.dao.domain.AbnormalChangeKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AbnormalChangeMapper {

    long countByExample(AbnormalChangeExample example);

    int deleteByExample(AbnormalChangeExample example);

    int deleteByPrimaryKey(AbnormalChangeKey key);

    int insert(AbnormalChange record);

    int insertSelective(AbnormalChange record);

    List<AbnormalChange> selectByExample(AbnormalChangeExample example);

    AbnormalChange selectByPrimaryKey(AbnormalChangeKey key);

    int updateByExampleSelective(@Param("record") AbnormalChange record, @Param("example") AbnormalChangeExample example);

    int updateByExample(@Param("record") AbnormalChange record, @Param("example") AbnormalChangeExample example);

    int updateByPrimaryKeySelective(AbnormalChange record);

    int updateByPrimaryKey(AbnormalChange record);

    List<AbnormalChange> select24hourHighLowAmount();
}