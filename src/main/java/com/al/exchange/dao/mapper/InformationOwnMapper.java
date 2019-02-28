package com.al.exchange.dao.mapper;

import com.al.exchange.dao.domain.InformationOwnDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InformationOwnMapper {

    @Select("select own.name as name ,own.symbol as symbol,own.kline_from as klineFrom,own.price_from as priceFrom, " +
            "own.day_volume as dayVolume,marketcap.name_self as nameSelf,own.market_cap_usd as cap, " +
            "own.available_supply as aSupply,own.total_supply as tSupply,own.rank,own.max_supply as mSupply,indicator_from as indicatorFrom from information_own own " +
            "LEFT JOIN  information_marketcap marketcap ON own.`name` = marketcap.name_own")
    List<InformationOwnDto> getInformationOwn();
}
